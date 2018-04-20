package application.service;

import org.springframework.stereotype.Service;

/**
 * This class contains the functionality for the metro transit system
 * @author mythili
 *
 */
@Service
public interface MetroService {

	/**
	 * This method retrieves all the Transit Data
	 * @throws Exception
	 */
	public void getTransitData() throws Exception;
	
}