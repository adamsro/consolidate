package adamsro;

import java.time.*;
import java.time.format.DateTimeParseException;

/**
 * Created by adamsro on 12/18/16.
 */
public class Entry implements Comparable {
    private String _id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
    private String entryDate;
    private ZonedDateTime entryDateObj;

    public Entry() {
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) throws DateTimeParseException {
        this.entryDate = entryDate;
        this.entryDateObj = ZonedDateTime.parse(entryDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        return this._id == ((Entry) o).get_id() && this.email == ((Entry) o).getEmail();
    }

    @Override
    public int compareTo(Object o) {
        return this.entryDate.compareTo(((Entry) o).getEntryDate());
    }
}
