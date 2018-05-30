package com.popframework.unex.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.popframework.unex.datamodel.FirstMatrixValues;
import com.popframework.unex.datamodel.SecondMatrixValues;
import com.popframework.unex.model.SimilarityCalculation;
import com.popframework.unex.model.SimilarityManageDB;

@RestController
public class SimilarityCalculationController {

	private SimilarityCalculation s = new SimilarityCalculation();
	private SimilarityManageDB sdb = SimilarityManageDB.getInstance();

	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(value = "/getConnection", method = RequestMethod.GET)
	public boolean connect() {
		sdb.connect();

		if (null != sdb.getConnection()) {
			return true;
		}

		return false;

	}

	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(value = "/procesar", method = RequestMethod.GET)
	public boolean procesar() {

		boolean result = s.procesar();
		if (result) {
			sdb.crearVistas();
		}

		return result;

	}

	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(value = "/firstMatrix", method = RequestMethod.GET)
	public FirstMatrixValues firstMatrix() {

		return s.calculateFirstMatrix();

	}

	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(value = "/secondMatrix", method = RequestMethod.GET)
	public SecondMatrixValues secondMatrix() {

		return s.calculateSecondMatrix();

	}

	/**
	 * desconectar base de datos
	 */

	@CrossOrigin(origins = "http://localhost:4200")
	@RequestMapping(value = "/disconnect", method = RequestMethod.GET)
	public void disconnect() {

		sdb.disconnect();

	}

}
