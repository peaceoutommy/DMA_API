package dev.tomas.dma.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.tomas.dma.dto.request.TicketCloseReq;
import dev.tomas.dma.entity.Ticket;
import dev.tomas.dma.enums.EntityType;
import dev.tomas.dma.enums.Status;
import dev.tomas.dma.repository.TicketRepo;
import dev.tomas.dma.service.ExternalStorageService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TicketControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TicketRepo ticketRepo;

    @MockitoBean
    private ExternalStorageService storageService;

    private Ticket openTicket;
    private Ticket closedTicket;

    @BeforeEach
    void setUp() {
        // Clean up all tickets
        ticketRepo.deleteAll();

        // Create open ticket
        openTicket = createTicketInDb("Open Ticket", Status.PENDING, EntityType.CAMPAIGN);

        // Create closed ticket
        closedTicket = createTicketInDb("Closed Ticket", Status.APPROVED, EntityType.COMPANY);
        closedTicket.setCloseDate(LocalDateTime.now());
        closedTicket = ticketRepo.save(closedTicket);
    }

    @AfterEach
    void tearDown() {
        ticketRepo.deleteAll();
    }

    // ==================== Get All Open Tickets Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets/open - Should return only open tickets")
    void getAllOpenTickets_ShouldReturnOpenTickets() throws Exception {
        // Create more open tickets
        createTicketInDb("Another Open Ticket", Status.PENDING, EntityType.USER);

        mockMvc.perform(get("/api/tickets/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)))
                .andExpect(jsonPath("$.tickets[*].status", everyItem(is("PENDING"))));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets/open - Should return empty when no open tickets")
    void getAllOpenTickets_NoOpenTickets_ShouldReturnEmpty() throws Exception {
        // Close all tickets
        ticketRepo.findAll().forEach(t -> {
            t.setStatus(Status.APPROVED);
            t.setCloseDate(LocalDateTime.now());
            ticketRepo.save(t);
        });

        mockMvc.perform(get("/api/tickets/open"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/tickets/open - Should return 401 without authentication")
    void getAllOpenTickets_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tickets/open"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get All Tickets Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets - Should return all tickets")
    void getAll_ShouldReturnAllTickets() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets - Should return empty when no tickets exist")
    void getAll_NoTickets_ShouldReturnEmpty() throws Exception {
        ticketRepo.deleteAll();

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/tickets - Should return 401 without authentication")
    void getAll_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Get Ticket By ID Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets/{ticketId} - Should return ticket by ID")
    void getByTicketId_ShouldReturnTicket() throws Exception {
        mockMvc.perform(get("/api/tickets/{ticketId}", openTicket.getId().intValue()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticket.id", is(openTicket.getId().intValue())))
                .andExpect(jsonPath("$.ticket.name", is(openTicket.getName())))
                .andExpect(jsonPath("$.ticket.status", is("PENDING")));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets/{ticketId} - Should return 404 for non-existent ticket")
    void getByTicketId_NonExistent_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/tickets/{ticketId}", 99999))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/tickets/{ticketId} - Should return 401 without authentication")
    void getByTicketId_WithoutAuth_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/tickets/{ticketId}", openTicket.getId().intValue()))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Close Ticket Tests ====================

    @Test
    @WithMockUser
    @DisplayName("PUT /api/tickets/close - Should close ticket with approved status")
    void closeTicket_WithApproved_ShouldCloseTicket() throws Exception {
        TicketCloseReq request = new TicketCloseReq();
        request.setId(openTicket.getId());
        request.setMessage("Ticket approved and closed");
        request.setStatus(Status.APPROVED);

        mockMvc.perform(put("/api/tickets/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify ticket was closed
        Ticket updated = ticketRepo.findById(openTicket.getId()).orElseThrow();
        Assertions.assertEquals(Status.APPROVED, updated.getStatus());
        Assertions.assertNotNull(updated.getCloseDate());
    }

    @Test
    @WithMockUser
    @DisplayName("PUT /api/tickets/close - Should close ticket with rejected status")
    void closeTicket_WithRejected_ShouldCloseTicket() throws Exception {
        TicketCloseReq request = new TicketCloseReq();
        request.setId(openTicket.getId());
        request.setMessage("Ticket rejected due to invalid request");
        request.setStatus(Status.REJECTED);

        mockMvc.perform(put("/api/tickets/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify ticket was closed
        Ticket updated = ticketRepo.findById(openTicket.getId()).orElseThrow();
        Assertions.assertEquals(Status.REJECTED, updated.getStatus());
    }

    @Test
    @DisplayName("PUT /api/tickets/close - Should return 401 without authentication")
    void closeTicket_WithoutAuth_ShouldReturn401() throws Exception {
        TicketCloseReq request = new TicketCloseReq();
        request.setId(openTicket.getId());
        request.setMessage("Test message");
        request.setStatus(Status.APPROVED);

        mockMvc.perform(put("/api/tickets/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==================== Ticket Entity Type Tests ====================

    @Test
    @WithMockUser
    @DisplayName("GET /api/tickets - Should return tickets of different entity types")
    void getAll_WithDifferentEntityTypes_ShouldReturnAll() throws Exception {
        // Create tickets for different entity types
        createTicketInDb("User Ticket", Status.PENDING, EntityType.USER);
        createTicketInDb("Fund Request Ticket", Status.PENDING, EntityType.FUND_REQUEST);

        mockMvc.perform(get("/api/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tickets", hasSize(4))) // 2 from setUp + 2 new ones
                .andExpect(jsonPath("$.tickets[*].type", hasItems("CAMPAIGN", "COMPANY", "USER", "FUND_REQUEST")));
    }

    // ==================== Helper Methods ====================

    private Ticket createTicketInDb(String name, Status status, EntityType type) {
        Ticket ticket = new Ticket();
        ticket.setName(name);
        ticket.setEntityId(1);
        ticket.setMessage("Test message for " + name);
        ticket.setAdditionalInfo("Additional info for testing");
        ticket.setCreateDate(LocalDateTime.now());
        ticket.setType(type);
        ticket.setStatus(status);
        return ticketRepo.save(ticket);
    }
}
