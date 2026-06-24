package info.fortheease.monastery360;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class EventsFragment extends Fragment {

    private CalendarView calendarView;
    private RecyclerView recyclerEvents;
    private EventsAdapter eventsAdapter;
    private List<Event> allEvents;

    public EventsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        calendarView = view.findViewById(R.id.calendarView);
        recyclerEvents = view.findViewById(R.id.recyclerEvents);

        // Generate dummy events
        allEvents = generateDummyEvents();

        // Setup RecyclerView
        recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        eventsAdapter = new EventsAdapter(allEvents);
        recyclerEvents.setAdapter(eventsAdapter);

        // Setup calendar markers
        List<EventDay> eventMarkers = new ArrayList<>(); // Renamed to avoid conflict
        for (Event event : allEvents) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(event.getYear(), event.getMonth(), event.getDay());
            // Using a consistent color for all event markers for now
            eventMarkers.add(new EventDay(calendar, R.drawable.baseline_event_24, Color.parseColor("#FF5722")));
        }
        calendarView.setEvents(eventMarkers);

        // Calendar date click listener
        calendarView.setOnDayClickListener(eventDay -> {
            Calendar clickedDayCalendar = eventDay.getCalendar();
            int year = clickedDayCalendar.get(Calendar.YEAR);
            int month = clickedDayCalendar.get(Calendar.MONTH);
            int day = clickedDayCalendar.get(Calendar.DAY_OF_MONTH);

            // Filter events for selected date
            List<Event> filtered = new ArrayList<>();
            for (Event e : allEvents) {
                if (e.getYear() == year && e.getMonth() == month && e.getDay() == day) {
                    filtered.add(e);
                }
            }
            eventsAdapter.updateEvents(filtered);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMonasterySpinnerEnabled(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setMonasterySpinnerEnabled(true);
        }
    }

    private List<Event> generateDummyEvents() {
        List<Event> events = new ArrayList<>();
        // Existing general events
        events.add(new Event("Global Monlam Prayer Festival", 2025, 8, 21, "A week-long prayer festival with international monks, rituals and offerings.")); // Sept 21
        events.add(new Event("Thousand Butter Lamps Lighting Ceremony", 2025, 8, 23, "Grand ceremony to light one thousand butter lamps for world peace.")); // Sept 23
        events.add(new Event("Inter-Monastic Debate Grand Finale", 2025, 8, 25, "Annual debate competition finals for monks and novices from various monasteries.")); // Sept 25
        events.add(new Event("Sacred Thanka Painting Exhibition", 2025, 8, 27, "Exhibition and workshop showcasing rare and traditional Tibetan Thanka painting techniques.")); // Sept 27

        // Phodong Monastery Events
        events.add(new Event("Phodong: Guthor Cham - Festival of Ritual Dances", 2025, 6, 12, "Opening ceremony of Phodong Monastery's annual Guthor Cham festival, featuring sacred dances.")); // July 12
        events.add(new Event("Phodong: Green Tara Intensive Meditation Retreat", 2025, 6, 18, "Commencement of a week-long Green Tara meditation retreat, guided by senior monks at Phodong.")); // July 18

        // Dubdi Monastery Events
        events.add(new Event("Dubdi: Founder's Day - Commemorating Lhatsun Namkha Jigme", 2025, 7, 7, "A series of talks and prayers on the history and founder of Dubdi, Sikkim's oldest monastery.")); // Aug 7
        events.add(new Event("Dubdi: Heritage Preservation & Manuscript Viewing", 2025, 7, 14, "Community update and blessing for the Dubdi restoration project, with a rare viewing of ancient manuscripts.")); // Aug 14

        // Rumtek Monastery Events
        events.add(new Event("Rumtek: Vajrakilaya Cham Dance Spectacle", 2025, 9, 5, "Vibrant and powerful Vajrakilaya masked dances depicting religious stories and subduing negative forces at Rumtek.")); // Oct 5
        events.add(new Event("Rumtek: Empowerment & Dharma Teachings by Rinpoche", 2025, 9, 10, "Special dharma teachings and empowerment ceremony by the head Rinpoche of Rumtek.")); // Oct 10

        // Lachung Monastery Events
        events.add(new Event("Lachung: Saga Dawa Duchen - Buddha's Enlightenment Day", 2025, 10, 1, "Celebrating Buddha's birth, enlightenment, and parinirvana with special prayers and events at Lachung.")); // Nov 1
        events.add(new Event("Lachung: Annual Harvest Blessing Festival & Community Feast", 2025, 10, 3, "A traditional community feast and harvest blessing ceremony at Lachung Monastery.")); // Nov 3

        // Enchey Monastery Events
        events.add(new Event("Enchey: Black Hat Dance (Detor Cham)", 2025, 11, 15, "First day of the annual Detor Cham featuring the mesmerizing Black Hat masked dances at Enchey.")); // Dec 15
        events.add(new Event("Enchey: Losar Tashi Delek - New Year Grand Celebration", 2026, 0, 1, "Special prayers and festivities to welcome the Tibetan New Year (Losar) at Enchey Monastery.")); // Jan 1, 2026

        return events;
    }
}
