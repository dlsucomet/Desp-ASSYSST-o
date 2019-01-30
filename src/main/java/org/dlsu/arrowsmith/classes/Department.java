package org.dlsu.arrowsmith.classes;

import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.Set;

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@Entity
@Audited(targetAuditMode = NOT_AUDITED)
public class Department {
    private Long deptId;
    private String deptName;
    private String deptCode;
    private College college;
    private Constraints constraints;
    private Set<Deloading> deloadings;
    private Set<FacultyLoad> facultyLoads;
    private Set<User> facultySet;

    public Department() {
    }

    public Department(Long deptId, String deptName, String deptCode) {
        this.deptId = deptId;
        this.deptName = deptName;
        this.deptCode = deptCode;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeptCode() {
        return deptCode;
    }

    public void setDeptCode(String deptCode) {
        this.deptCode = deptCode;
    }

    @ManyToOne
    @JoinColumn(name = "collegeId")
    public College getCollege() {
        return college;
    }

    public void setCollege(College college) {
        this.college = college;
    }

    @OneToOne(mappedBy = "department")
    public Constraints getConstraints() {
        return constraints;
    }

    public void setConstraints(Constraints constraints) {
        this.constraints = constraints;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    public Set<Deloading> getDeloadings() {
        return deloadings;
    }

    public void setDeloadings(Set<Deloading> deloadings) {
        this.deloadings = deloadings;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    public Set<FacultyLoad> getFacultyLoads() {
        return facultyLoads;
    }

    public void setFacultyLoads(Set<FacultyLoad> facultyLoads) {
        this.facultyLoads = facultyLoads;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    public Set<User> getFacultySet() {
        return facultySet;
    }

    public void setFacultySet(Set<User> facultySet) {
        this.facultySet = facultySet;
    }
}
