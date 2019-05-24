package pe.com.acme.worflowacme.dto;

import lombok.*;

/**
 * Created by Ivan on 22/05/2019.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatientDTO {

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
