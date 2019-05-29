package pe.com.acme.worflowacme.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by Ivan on 23/05/2019.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(of = {"sourceId", "socialSecurityNumber"})
@Entity
@Table(name = "PATIENT")
public class PatientEntity {

    @Id
    @GeneratedValue(generator = "SQ_PATIENT_001", strategy = GenerationType.SEQUENCE)
    @SequenceGenerator(name = "SQ_PATIENT_001", sequenceName = "SQ_PATIENT_001", allocationSize = 1)
    @Column(name = "PATIENT_ID")
    private Long patientId;

    @Column(name = "SOURCE_ID")
    private String sourceId;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_INITIAL")
    private String middleInitial;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "PHONE_NUMBER")
    private String phoneNumber;

    @Column(name = "STREET")
    private String street;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STATE")
    private String state;

    @Column(name = "ZIP_CODE")
    private String zipCode;

    @Column(name = "BIRTH_DATE")
    private LocalDate birthDate;

    @Column(name = "SOCIAL_SECURITY_NUMBER")
    private String socialSecurityNumber;

    @Column(name = "CREATION_DATE_TIME")
    private LocalDateTime creationDateTime;

}
