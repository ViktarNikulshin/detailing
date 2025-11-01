package com.nikulshin.detailing.mapper;

import com.nikulshin.detailing.model.domain.Order;
import com.nikulshin.detailing.model.domain.OrderStatus;
import com.nikulshin.detailing.model.domain.User;
import com.nikulshin.detailing.model.domain.Work;
import com.nikulshin.detailing.model.domain.WorkMasterAssignment;
import com.nikulshin.detailing.model.dto.report.MasterDetailEarningDto;
import com.nikulshin.detailing.model.dto.report.MasterDetailReportDto;
import com.nikulshin.detailing.model.dto.report.MasterWeeklyReportDto;
import com.nikulshin.detailing.model.dto.report.MasterWorkTypeEarningDto;
import com.nikulshin.detailing.model.dto.report.OrderEarningDto;
import com.nikulshin.detailing.model.helper.MasterEarningsSummary;
import com.nikulshin.detailing.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ReportMapper {
    private final OrderRepository orderRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public List<MasterWeeklyReportDto> getWeeklyReport(LocalDateTime start, LocalDateTime end) {
        // 1. Сбор всех WorkMasterAssignment за период
        List<Order> orders = orderRepository.findByStatusAndExecutionDateBetween(OrderStatus.COMPLETED, start, end);
        List<Work> works = orders
                .stream()
                .flatMap(o -> o.getWorks().stream())
                .toList();
        List<WorkMasterAssignment> workMasterAssignments = works
                .stream()
                .flatMap(w -> w.getAssignments().stream())
                .toList();

        // 2. Группировка и расчет заработка по мастерам
        Map<Long, MasterEarningsSummary> masterEarningsMap = calculateMasterEarnings(workMasterAssignments);

        // 3. Маппинг сгруппированных данных в List<MasterWeeklyReportDto>
        return masterEarningsMap.values().stream() // Итерируем по объектам MasterEarningsSummary
                .map(summary -> {
                    User master = summary.getMaster(); // Получаем объект мастера из Summary

                    // 3a. Маппинг внутренней Map<String, Double> в List<MasterWorkTypeEarningDto>
                    List<MasterWorkTypeEarningDto> earningsDtoList = summary.getTotalEarningsByWorkType().entrySet().stream()
                            .map(entry -> MasterWorkTypeEarningDto.builder()
                                    // workTypeId не доступен после группировки по имени, поэтому опускаем его или передаем null
                                    // .workTypeId(...)
                                    .workTypeName(entry.getKey())
                                    .totalEarnings(entry.getValue())
                                    .build())
                            .toList();

                    // 3b. Построение итогового DTO
                    return MasterWeeklyReportDto.builder()
                            .masterId(master.getId().intValue()) // Используем данные из User
                            .masterFirstName(master.getFirstName())
                            .masterLastName(master.getLastName())
                            .earnings(earningsDtoList)
                            .totalMasterEarnings(summary.getTotalMasterEarnings()) // Получаем общий заработок
                            .build();
                })
                .toList();
    }

    public Map<Long, MasterEarningsSummary> calculateMasterEarnings(
            List<WorkMasterAssignment> assignments) {

        return assignments.stream()
                .collect(Collectors.groupingBy(
                        assignment -> assignment.getMaster().getId(),
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                assignmentList -> {
                                    MasterEarningsSummary summary = new MasterEarningsSummary();
                                    if (!assignmentList.isEmpty()) {
                                        // Сохраняем мастера, чтобы получить его ФИО
                                        summary.setMaster(assignmentList.get(0).getMaster());
                                    }
                                    for (WorkMasterAssignment assignment : assignmentList) {
                                        String workTypeName = assignment.getWork().getWorkType().getName();
                                        Double cost = (double) assignment.getWork().getCost();
                                        Double percent = assignment.getSalaryPercent();
                                        Double earnings = cost * percent / 100.0;
                                        summary.addEarnings(workTypeName, earnings);
                                    }
                                    return summary;
                                }
                        )
                ));
    }

    public MasterDetailReportDto getMasterDetailReport(
            Long masterId,
            LocalDateTime start,
            LocalDateTime end) {

        // 1. Сбор всех WorkMasterAssignment за период
        List<Order> orders = orderRepository.findByStatusAndExecutionDateBetween(OrderStatus.COMPLETED, start, end);
        List<Work> works = orders
                .stream()
                .flatMap(o -> o.getWorks().stream())
                .toList();
        List<WorkMasterAssignment> workMasterAssignments = works
                .stream()
                .flatMap(w -> w.getAssignments().stream())
                // 2. Фильтрация по конкретному мастеру
                .filter(a -> a.getMaster().getId().equals(masterId))
                .toList();

        if (workMasterAssignments.isEmpty()) {
            // Если заданий нет, возвращаем пустой DTO или DTO только с данными мастера
            return MasterDetailReportDto.builder()
                    .masterId(masterId.intValue())
                    // Если нужен masterFirstName/masterLastName, его придется получать через UserRepository,
                    // но для простоты здесь оставим пустым.
                    .reportDetails(List.of())
                    .build();
        }

        WorkMasterAssignment firstAssignment = workMasterAssignments.get(0);

        // 3. Группировка по типу работы и сбор в MasterDetailEarningDto
        Map<Long, List<WorkMasterAssignment>> assignmentsByWorkType =
                workMasterAssignments.stream()
                        .collect(Collectors.groupingBy(a -> a.getWork().getWorkType().getId()));

        List<MasterDetailEarningDto> reportDetails = assignmentsByWorkType.entrySet().stream()
                .map(entry -> {
                    // entry.getKey() = workType.id
                    List<WorkMasterAssignment> typeAssignments = entry.getValue();
                    WorkMasterAssignment sampleAssignment = typeAssignments.get(0);

                    // 3a. Сбор заработка по заказам (OrderEarningDto)
                    List<OrderEarningDto> earningsByOrder = typeAssignments.stream()
                            .map(assignment -> {
                                Order order = assignment.getWork().getOrder();
                                Double cost = (double) assignment.getWork().getCost();
                                Double percent = assignment.getSalaryPercent();
                                Double earning = cost * percent / 100.0;

                                return OrderEarningDto.builder()
                                        .orderId(order.getId().intValue())
                                        .clientName(order.getClientName())
                                        .clientCar(order.getCarBrand().getName())
                                        .executionDate(order.getExecutionDate().format(DATE_FORMATTER))
                                        .earning(earning)
                                        .build();
                            })
                            .toList();

                    // 3b. Построение MasterDetailEarningDto
                    return MasterDetailEarningDto.builder()
                            .workTypeId(sampleAssignment.getWork().getWorkType().getId().intValue())
                            .workTypeName(sampleAssignment.getWork().getWorkType().getName())
                            .earningsByOrder(earningsByOrder)
                            .build();
                })
                .toList();

        // 4. Построение итогового MasterDetailReportDto
        return MasterDetailReportDto.builder()
                .masterId(firstAssignment.getMaster().getId().intValue())
                .masterFirstName(firstAssignment.getMaster().getFirstName())
                .masterLastName(firstAssignment.getMaster().getLastName())
                .reportDetails(reportDetails)
                .build();
    }
}
