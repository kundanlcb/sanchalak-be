package com.cm.sanchalak.controller;

import com.cm.sanchalak.entity.*;
import com.cm.sanchalak.service.*;
import com.cm.sanchalak.security.UserPrincipal;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import com.cm.sanchalak.security.CurrentUser;
import com.cm.sanchalak.repository.UserRepository;
import com.cm.sanchalak.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransportControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TransportController transportController;

    @Mock
    private TransportService transportService;
    @Mock
    private LocationTrackingService locationTrackingService;
    @Mock
    private TransportEtaService transportEtaService;
    @Mock
    private TransportEventService transportEventService;
    @Mock
    private ParentAuthorizationService parentAuthService;
    @Mock
    private ParentService parentService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private StudentRepository studentRepository;

    @BeforeEach
    void setUp() {
        // Will be overridden in test
    }

    @Test
    void testGetMyRoute_Student() throws Exception {
        // Mock data
        UUID userId = UUID.randomUUID();
        Long studentId = 1L;
        Long routeId = 10L;

        UserPrincipal principal = new UserPrincipal(userId, "Student Name", "student", "student@example.com",
                "password",
                null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_STUDENT")));

        // Setup Standalone MockMvc with a custom ArgumentResolver to inject our
        // principal
        mockMvc = MockMvcBuilders.standaloneSetup(transportController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(UserPrincipal.class)
                                || parameter.hasParameterAnnotation(CurrentUser.class);
                    }

                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                            NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                        System.out.println("Resolving UserPrincipal: " + principal.getId());
                        return principal;
                    }
                })
                .build();

        Student student = new Student();
        student.setId(studentId);
        student.setUserId(userId);
        // Ensure active status if needed, though controller doesn't seem to check it
        // explicitly yet

        Role role = new Role();
        role.setName(RoleName.ROLE_STUDENT);

        User user = new User();
        user.setId(userId);
        user.setName("Test Student");
        user.setEmail("student@example.com");
        user.setRoles(Collections.singleton(role));

        // Strict stubbing check: make sure the arguments match EXACTLY what the
        // controller receives
        when(userRepository.findById(eq(userId))).thenReturn(Optional.of(user));

        // When user is found, controller checks role. If student, calls findByUserId
        // with user.getId() (which is userId)
        when(studentRepository.findByUserId(eq(userId))).thenReturn(Optional.of(student));

        Route route = new Route();
        route.setId(routeId);
        route.setRouteName("Route 10");

        Vehicle vehicle = new Vehicle();
        vehicle.setVehicleNumber("BUS-001");
        route.setVehicle(vehicle);

        Stop stop = new Stop();
        stop.setStopName("Home Stop");

        StudentTransportAssignment assignment = new StudentTransportAssignment();
        assignment.setRoute(route);
        assignment.setStop(stop);

        when(transportService.getActiveAssignmentForStudent(studentId)).thenReturn(assignment);
        when(transportService.getStopsByRouteId(routeId)).thenReturn(Collections.singletonList(stop));
        when(transportService.getActiveTripForRoute(eq(routeId), any(LocalDate.class))).thenReturn(null);

        // Execute
        mockMvc.perform(get("/api/transport/my-route")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.routeId").value(routeId.intValue()))
                .andExpect(jsonPath("$.data.routeName").value("Route 10"));
    }
}
