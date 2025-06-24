package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Extracurricular {

    private final IntegerProperty extracurricularId;
    private final StringProperty extracurricularName;
    private final StringProperty day;
    private LocalTime time;
    private final StringProperty extracurricularPlace;

    public Extracurricular(int id, String name, String day, LocalTime time, String place) {
        this.extracurricularId = new SimpleIntegerProperty(id);
        this.extracurricularName = new SimpleStringProperty(name);
        this.day = new SimpleStringProperty(day);
        this.time = time;
        this.extracurricularPlace = new SimpleStringProperty(place);
    }

    // Getters
    public int getExtracurricularId() { return extracurricularId.get(); }
    public String getExtracurricularName() { return extracurricularName.get(); }
    public String getDay() { return day.get(); }
    public String getTimeFormatted() {
        if (time != null) {
            return time.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
        return "-";
    }
    public LocalTime getTime() { return time; }
    public String getExtracurricularPlace() { return extracurricularPlace.get(); }

    // Property Getters for TableView
    public IntegerProperty extracurricularIdProperty() { return extracurricularId; }
    public StringProperty extracurricularNameProperty() { return extracurricularName; }
    public StringProperty dayProperty() { return day; }
    public StringProperty extracurricularPlaceProperty() { return extracurricularPlace; }

    @Override
    public String toString() {
        return getExtracurricularName(); // Penting untuk ComboBox atau ListView
    }
}