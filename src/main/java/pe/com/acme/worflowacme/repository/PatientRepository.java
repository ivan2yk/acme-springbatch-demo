package pe.com.acme.worflowacme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.com.acme.worflowacme.domain.PatientEntity;

/**
 * Created by Ivan on 28/05/2019.
 */
@Repository
public interface PatientRepository extends JpaRepository<PatientEntity, Long> {

}
