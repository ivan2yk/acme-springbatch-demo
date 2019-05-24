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
    private String sourceId;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private LocalDate birthDate;
    private String socialSecurityNumber;
    private LocalDateTime creationDateTime;

}
