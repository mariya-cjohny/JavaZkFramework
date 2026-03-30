package com.sample.PersonApp.viewModel;

import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Messagebox;

import com.sample.PersonApp.dao.PersonDAO;
import com.sample.PersonApp.dto.PersonDTO;
import com.sample.PersonApp.dto.UserDTO;

public class HomeViewModel {

    private PersonDTO person;
    private List<PersonDTO> persons;
    private boolean editMode = false;
    private String username;
    private String role;
    private String nameError;
    private String ageError;
    private String salaryError;
    private String searchText;
    private List<PersonDTO> allPersons;
    private int pageSize = 10;
    private int activePage = 0;
    private int totalSize;

    private PersonDAO dao = new PersonDAO();

    @Init
    public void init() {
        person = new PersonDTO();
        allPersons = dao.getAll();
        totalSize = allPersons.size();
        loadPage();
        UserDTO user = (UserDTO) Sessions.getCurrent().getAttribute("user");
        if (user != null) {
            username = user.getUsername();
            role = user.getRole();
        }
    }

    private void loadPage() {
        int start = activePage * pageSize;
        int end = Math.min(start + pageSize, allPersons.size());

        if (start >= allPersons.size()) {
            persons = List.of();
            return;
        }

        persons = allPersons.subList(start, end);
    }

    @Command
    @NotifyChange({"persons", "activePage", "totalPages"})
    public void changePage(@BindingParam("page") int page) {

        if (page < 0) {
            page = 0;
        }

        if (page >= getTotalPages()) {
            page = getTotalPages() - 1;
        }

        this.activePage = page;
        loadPage();
    }

    public int getTotalPages() {
        if (totalSize == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalSize / pageSize);
    }

    public PersonDTO getPerson() {
        return person;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<PersonDTO> getPersons() {
        return persons;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(role);
    }

    public String getUsername() {
        return username;
    }

    public String getNameError() {
        return nameError;
    }

    public String getAgeError() {
        return ageError;
    }

    public String getSalaryError() {
        return salaryError;
    }

    @Command
    @NotifyChange({"persons", "person", "editMode", "nameError", "ageError", "salaryError", "highestSalaryPerson", "topEarnerAvailable", "topEarnerName", "topEarnerSalary"})
    public void save() {

        nameError = null;
        ageError = null;
        salaryError = null;
        boolean hasError = false;

        if (person.getName() == null || person.getName().trim().isEmpty()) {
            nameError = "Name is required";
            hasError = true;
        }

        if (person.getAge() == null || person.getAge() <= 0) {
            ageError = "Valid age is required";
            hasError = true;
        }

        if (person.getSalary() == null || person.getSalary() <= 0) {
            salaryError = "Salary must be > 0";
            hasError = true;
        }

        // Name format
        if (person.getName() != null && !person.getName().matches("[a-zA-Z ]+")) {
            nameError = "Name should contain only letters";
            hasError = true;
        }
        // Age range
        if (person.getAge() != null && person.getAge() < 18) {
            ageError = "Minimum age is 18";
            hasError = true;
        }

        if (person.getAge() != null && person.getAge() > 60) {
            ageError = "Age cannot exceed 60";
            hasError = true;
        }

        // Salary limit
        if (person.getSalary() != null && person.getSalary() > 10000000) {
            salaryError = "Salary cannot exceed 10000000";
            hasError = true;
        }

        if (!isAdmin()) {
            Clients.showNotification("Access Denied!", "error", null, "top_right", 3000);
            return;
        }

        if (hasError) {
            return;
        }
        try {
            if (!editMode) {
                dao.insert(person);
                Clients.showNotification("User added successfully!", "info", null, "top_right", 4000);
            } else {
                dao.update(person);
                editMode = false;
                Clients.showNotification("User updated successfully!", "info", null, "top_right", 4000);
            }

            person = new PersonDTO();
            allPersons = dao.getAll();
            totalSize = allPersons.size();
            activePage = 0;
            loadPage();
            BindUtils.postNotifyChange(null, null, this, "persons");
            BindUtils.postNotifyChange(null, null, this, "totalPages");
            BindUtils.postNotifyChange(null, null, this, "activePage");

        } catch (Exception e) {
            Clients.showNotification("Error occurred!", "error", null, "top_right", 4000);
            e.printStackTrace();
        }
    }

    @Command
    @NotifyChange({"persons", "highestSalaryPerson", "topEarnerAvailable", "topEarnerName", "topEarnerSalary"})
    public void delete(@BindingParam("each") PersonDTO p) {
        if (!isAdmin()) {
            Clients.showNotification("Access Denied!", "error", null, "top_right", 3000);
            return;
        }
        org.zkoss.zul.Messagebox.show(
                "Are you sure you want to delete?",
                "Confirm Delete",
                org.zkoss.zul.Messagebox.YES | org.zkoss.zul.Messagebox.NO,
                org.zkoss.zul.Messagebox.QUESTION,
                event -> {
                    if (event.getName().equals(org.zkoss.zul.Messagebox.ON_YES)) {
                        dao.delete(p.getId());
                        allPersons = dao.getAll();
                        totalSize = allPersons.size();
                        loadPage();
                        BindUtils.postNotifyChange(null, null, this, "persons");
                        BindUtils.postNotifyChange(null, null, this, "highestSalaryPerson");
                        BindUtils.postNotifyChange(null, null, this, "topEarnerAvailable");
                        BindUtils.postNotifyChange(null, null, this, "topEarnerName");
                        BindUtils.postNotifyChange(null, null, this, "topEarnerSalary");
                        Clients.showNotification("User deleted successfully!", "warning", null, "top_right", 4000);
                    }
                }
        );
    }

    @Command
    @NotifyChange({"person", "editMode"})
    public void edit(@BindingParam("each") PersonDTO p) {
        if (!isAdmin()) {
            Clients.showNotification("Access Denied!", "error", null, "top_right", 3000);
            return;
        }
        PersonDTO temp = new PersonDTO();
        temp.setId(p.getId());
        temp.setName(p.getName());
        temp.setAge(p.getAge());
        temp.setSalary(p.getSalary());

        this.person = temp;
        editMode = true;
    }

    @Command
    public void checkSalary(@BindingParam("salary") Double salary) {
        if (salary == null) {
            org.zkoss.zul.Messagebox.show("Please enter salary first");
            return;
        }

        if (salary > 5000) {
            Messagebox.show(
                    "Great! Salary is above 5000",
                    "Success",
                    Messagebox.OK,
                    Messagebox.INFORMATION
            );
        }
    }

    @Command
    public void logout() {
        Sessions.getCurrent().invalidate();
        Executions.sendRedirect("login.zul");
    }

    @Command
    @NotifyChange("nameError")
    public void clearNameError() {
        nameError = null;
    }

    @Command
    @NotifyChange("ageError")
    public void clearAgeError() {
        ageError = null;
    }

    @Command
    @NotifyChange("salaryError")
    public void clearSalaryError() {
        salaryError = null;
    }

    public String getWelcomeMessage() {
        return "Welcome " + (username != null ? username : "User");
    }

    public PersonDTO getHighestSalaryPerson() {
        if (persons == null || persons.isEmpty()) {
            return null;
        }

        return persons.stream()
                .filter(p -> p.getSalary() != null)
                .max((p1, p2) -> Double.compare(p1.getSalary(), p2.getSalary()))
                .orElse(null);
    }

    public boolean isTopEarnerAvailable() {
        return getHighestSalaryPerson() != null;
    }

    public String getTopEarnerName() {
        PersonDTO p = getHighestSalaryPerson();
        return p != null ? p.getName() : "-";
    }

    public String getTopEarnerSalary() {
        PersonDTO p = getHighestSalaryPerson();
        return p != null ? String.valueOf(p.getSalary()) : "-";
    }

    @Command
    @NotifyChange({"person", "editMode", "nameError", "ageError", "salaryError"})
    public void cancel() {

        boolean isEmpty
                = (person.getName() == null || person.getName().trim().isEmpty())
                && person.getAge() == null
                && person.getSalary() == null;

        if (isEmpty) {
            person = new PersonDTO();
            editMode = false;
            nameError = null;
            ageError = null;
            salaryError = null;
            return;
        }

        Messagebox.show(
                "Are you sure you want to clear the form?",
                "Confirm",
                Messagebox.YES | Messagebox.NO,
                Messagebox.QUESTION,
                event -> {
                    if (event.getName().equals(Messagebox.ON_YES)) {
                        person = new PersonDTO();
                        editMode = false;
                        nameError = null;
                        ageError = null;
                        salaryError = null;

                        BindUtils.postNotifyChange(null, null, this, "person");
                        BindUtils.postNotifyChange(null, null, this, "editMode");
                        BindUtils.postNotifyChange(null, null, this, "nameError");
                        BindUtils.postNotifyChange(null, null, this, "ageError");
                        BindUtils.postNotifyChange(null, null, this, "salaryError");
                    }
                }
        );
    }

    @Command
    @NotifyChange({"persons", "noRecordsFound", "totalPages", "activePage"})
    public void search() {
        if (searchText == null || searchText.trim().isEmpty()) {
            allPersons = dao.getAll();
        } else {
            String searchLower = searchText.toLowerCase();

            allPersons = dao.getAll().stream()
                    .filter(p -> p.getName() != null
                    && p.getName().toLowerCase().contains(searchLower))
                    .toList();
        }

        activePage = 0; // reset page
        totalSize = allPersons.size();
        loadPage();
        BindUtils.postNotifyChange(null, null, this, "persons");
        BindUtils.postNotifyChange(null, null, this, "totalPages");
        BindUtils.postNotifyChange(null, null, this, "activePage");
    }

    @Command
    @NotifyChange({"searchText", "persons", "noRecordsFound", "totalPages", "activePage"})
    public void clearSearch() {
        searchText = null;
        allPersons = dao.getAll();
        activePage = 0;
        totalSize = allPersons.size();
        loadPage();
    }

    public boolean isNoRecordsFound() {
        return persons == null || persons.isEmpty();
    }

    public int getActivePage() {
        return activePage;
    }

}
