package pe.com.acme.worflowacme.dto;

import lombok.*;

import java.io.Serializable;

/**
 * Created by Ivan on 22/05/2019.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString(of = {"sourceId", "firstName", "lastName"})
public class PatientDTO implements Serializable {

    private static final long serialVersionUID = 2574063110603295300L;
    private String sourceId;
    private String firstName;
    private String middleInitial;
    private String lastName;
    private String emailAddress;
    private String phoneNumber;
    private String street;
    private String city;
    private String state;
    private String zip;
    private String birthdate;
    private String action;
    private String ssn;

}
