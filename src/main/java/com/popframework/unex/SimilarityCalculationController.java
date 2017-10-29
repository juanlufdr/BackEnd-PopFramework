package com.popframework.unex;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SimilarityCalculationController {
	
	private SimilarityCalculationPrueba s = new SimilarityCalculationPrueba();
	
	
	@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value="/getConnection", method=RequestMethod.GET)
    public void connect() {
    	s.connect();
    	
    	//TODO: comprobar conexion;
     
    }
	
	@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value="/firstMatrix", method=RequestMethod.GET)
    public SetOfValues firstMatrix() {
    	
    	s.connect();
    	return s.calculateFirstMatrix();
     
    }
    
	@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value="/secondMatrix", method=RequestMethod.GET)
    public SecondMatrixValues secondMatrix() {
    	
    	s.connect();
    	return s.calculateSecondMatrix();
     
    }
    
	@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value="/disconnect", method=RequestMethod.GET)
    public void disconnect() {
    	
    	s.disconnect();
    	
    	//TODO: comprobar desconexion
     
    }
    
	@CrossOrigin(origins = "http://localhost:4200")
    @RequestMapping(value="/greeting", method=RequestMethod.GET)
    public Greeting gretiin() {
    	
    	return s.sayHello();
    	
    	//TODO: comprobar conexion;
     
    }
    
    

}
