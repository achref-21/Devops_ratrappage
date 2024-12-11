package tn.esprit.eventsproject.services;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tn.esprit.eventsproject.entities.Event;
import tn.esprit.eventsproject.entities.Logistics;
import tn.esprit.eventsproject.entities.Participant;
import tn.esprit.eventsproject.entities.Tache;
import tn.esprit.eventsproject.repositories.EventRepository;
import tn.esprit.eventsproject.repositories.LogisticsRepository;
import tn.esprit.eventsproject.repositories.ParticipantRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServicesImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private LogisticsRepository logisticsRepository;

    @InjectMocks
    private EventServicesImpl eventServices;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddParticipant() {
        Participant participant = new Participant();
        when(participantRepository.save(participant)).thenReturn(participant);

        Participant savedParticipant = eventServices.addParticipant(participant);

        assertNotNull(savedParticipant);
        verify(participantRepository, times(1)).save(participant);
    }

    @Test
    void testAddAffectEvenParticipant_WithId() {
        Participant participant = new Participant();
        participant.setEvents(new HashSet<>());
        when(participantRepository.findById(1)).thenReturn(Optional.of(participant));

        Event event = new Event();
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event, 1);

        assertNotNull(savedEvent);
        assertTrue(participant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectEvenParticipant_WithParticipants() {
        Event event = new Event();
        Participant participant = new Participant();
        participant.setIdPart(1);
        event.setParticipants(Set.of(participant));

        Participant fetchedParticipant = new Participant();
        fetchedParticipant.setEvents(new HashSet<>());
        when(participantRepository.findById(1)).thenReturn(Optional.of(fetchedParticipant));
        when(eventRepository.save(event)).thenReturn(event);

        Event savedEvent = eventServices.addAffectEvenParticipant(event);

        assertNotNull(savedEvent);
        assertTrue(fetchedParticipant.getEvents().contains(event));
        verify(participantRepository, times(1)).findById(1);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testAddAffectLog() {
        Event event = new Event();
        event.setLogistics(new HashSet<>());
        when(eventRepository.findByDescription("Event1")).thenReturn(event);

        Logistics logistics = new Logistics();
        when(logisticsRepository.save(logistics)).thenReturn(logistics);

        Logistics savedLogistics = eventServices.addAffectLog(logistics, "Event1");

        assertNotNull(savedLogistics);
        assertTrue(event.getLogistics().contains(logistics));
        verify(eventRepository, times(1)).findByDescription("Event1");
        verify(logisticsRepository, times(1)).save(logistics);
    }

    @Test
    void testGetLogisticsDates() {
        Event event = new Event();
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        event.setLogistics(Set.of(logistics));

        when(eventRepository.findByDateDebutBetween(LocalDate.now(), LocalDate.now().plusDays(1)))
                .thenReturn(List.of(event));

        List<Logistics> logisticsList = eventServices.getLogisticsDates(LocalDate.now(), LocalDate.now().plusDays(1));

        assertNotNull(logisticsList);
        assertEquals(1, logisticsList.size());
        assertTrue(logisticsList.contains(logistics));
        verify(eventRepository, times(1)).findByDateDebutBetween(any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    void testCalculCout() {
        Event event = new Event();
        event.setDescription("Event1");
        Logistics logistics = new Logistics();
        logistics.setReserve(true);
        logistics.setPrixUnit(100);
        logistics.setQuantite(2);
        event.setLogistics(Set.of(logistics));

        when(eventRepository.findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                "Tounsi", "Ahmed", Tache.ORGANISATEUR)).thenReturn(List.of(event));

        eventServices.calculCout();

        verify(eventRepository, times(1)).findByParticipants_NomAndParticipants_PrenomAndParticipants_Tache(
                eq("Tounsi"), eq("Ahmed"), eq(Tache.ORGANISATEUR));
        verify(eventRepository, times(1)).save(event);

        assertEquals(200, event.getCout());
    }
}
