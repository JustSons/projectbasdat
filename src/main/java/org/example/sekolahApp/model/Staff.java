package org.example.sekolahApp.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.time.LocalDate;

public class Staff {
    private final IntegerProperty staffId;
    private final StringProperty staffName;
    private final StringProperty staffPhoneNumber;
    private final StringProperty staffEmail;
    private final StringProperty staffAddress;
    private LocalDate staffHireDate;
    
    // Additional fields for compatibility with existing project
    private final StringProperty nip; // Keep for backward compatibility
    private final StringProperty jabatan; // Keep for role display

    public Staff(int staffId, String staffName, String staffPhoneNumber, String staffEmail, String staffAddress, LocalDate staffHireDate) {
        this.staffId = new SimpleIntegerProperty(staffId);
        this.staffName = new SimpleStringProperty(staffName != null ? staffName : "");
        this.staffPhoneNumber = new SimpleStringProperty(staffPhoneNumber != null ? staffPhoneNumber : "");
        this.staffEmail = new SimpleStringProperty(staffEmail != null ? staffEmail : "");
        this.staffAddress = new SimpleStringProperty(staffAddress != null ? staffAddress : "");
        this.staffHireDate = staffHireDate;
        
        // Compatibility fields
        this.nip = new SimpleStringProperty("");
        this.jabatan = new SimpleStringProperty("");
    }

    // Constructor with legacy fields for backward compatibility
    public Staff(int staffId, String nip, String staffName, String jabatan, String staffEmail, String staffPhoneNumber, String staffAddress, LocalDate staffHireDate) {
        this.staffId = new SimpleIntegerProperty(staffId);
        this.staffName = new SimpleStringProperty(staffName != null ? staffName : "");
        this.staffPhoneNumber = new SimpleStringProperty(staffPhoneNumber != null ? staffPhoneNumber : "");
        this.staffEmail = new SimpleStringProperty(staffEmail != null ? staffEmail : "");
        this.staffAddress = new SimpleStringProperty(staffAddress != null ? staffAddress : "");
        this.staffHireDate = staffHireDate;
        
        // Compatibility fields
        this.nip = new SimpleStringProperty(nip != null ? nip : "");
        this.jabatan = new SimpleStringProperty(jabatan != null ? jabatan : "");
    }

    // Constructor simplified for ComboBox display
    public Staff(int staffId, String staffName) {
        this(staffId, staffName, null, null, null, null);
    }

    // Primary getters (following schema)
    public int getStaffId() { return staffId.get(); }
    public String getStaffName() { return staffName.get(); }
    public String getStaffPhoneNumber() { return staffPhoneNumber.get(); }
    public String getStaffEmail() { return staffEmail.get(); }
    public String getStaffAddress() { return staffAddress.get(); }
    public LocalDate getStaffHireDate() { return staffHireDate; }

    // Legacy getters for compatibility
    public String getNip() { return nip.get(); }
    public String getNamaStaff() { return staffName.get(); } // Alias for legacy code
    public String getJabatan() { return jabatan.get(); }
    public String getEmail() { return staffEmail.get(); } // Alias
    public String getNomorTelepon() { return staffPhoneNumber.get(); } // Alias
    public String getAlamat() { return staffAddress.get(); } // Alias
    public LocalDate getTanggalLahir() { return staffHireDate; } // Alias (semantically different but for compatibility)

    // Primary setters
    public void setStaffName(String staffName) { this.staffName.set(staffName); }
    public void setStaffPhoneNumber(String staffPhoneNumber) { this.staffPhoneNumber.set(staffPhoneNumber); }
    public void setStaffEmail(String staffEmail) { this.staffEmail.set(staffEmail); }
    public void setStaffAddress(String staffAddress) { this.staffAddress.set(staffAddress); }
    public void setStaffHireDate(LocalDate staffHireDate) { this.staffHireDate = staffHireDate; }
    
    // Legacy setters
    public void setNip(String nip) { this.nip.set(nip); }
    public void setJabatan(String jabatan) { this.jabatan.set(jabatan); }

    // Property getters (for TableView binding)
    public IntegerProperty staffIdProperty() { return staffId; }
    public StringProperty staffNameProperty() { return staffName; }
    public StringProperty staffPhoneNumberProperty() { return staffPhoneNumber; }
    public StringProperty staffEmailProperty() { return staffEmail; }
    public StringProperty staffAddressProperty() { return staffAddress; }
    
    // Legacy property getters
    public StringProperty nipProperty() { return nip; }
    public StringProperty namaStaffProperty() { return staffName; } // Alias
    public StringProperty jabatanProperty() { return jabatan; }
    public StringProperty emailProperty() { return staffEmail; } // Alias
    public StringProperty nomorTeleponProperty() { return staffPhoneNumber; } // Alias
    public StringProperty alamatProperty() { return staffAddress; } // Alias

    @Override
    public String toString() {
        return getStaffName(); // For ComboBox display
    }
}