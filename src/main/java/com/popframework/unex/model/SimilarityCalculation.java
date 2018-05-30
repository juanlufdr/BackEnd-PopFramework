package com.popframework.unex.model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import com.popframework.unex.datamodel.CollectionValue;
import com.popframework.unex.datamodel.FirstMatrixValues;
import com.popframework.unex.datamodel.SecondMatrixValues;

import net.ricecode.similarity.JaroWinklerStrategy;
import net.ricecode.similarity.SimilarityStrategy;
import net.ricecode.similarity.StringSimilarityService;
import net.ricecode.similarity.StringSimilarityServiceImpl;

public class SimilarityCalculation {

	private final String FILENAME2 = "input/categories.txt";
	private final String FILENAME3 = "input/identificadores.txt";

	private SimilarityManageDB manageDB;

	public int contador = 0;

	public SimilarityCalculation() {
		manageDB = SimilarityManageDB.getInstance();
	}

	/**
	 * Realiza la comparación semántica entre dos cadenas mediante el algoritmo de
	 * Jaro-Winkler
	 * 
	 * @param p1
	 *            primera a comparar
	 * @param p2
	 *            segund a comparar
	 * @return double de 0 a 1 que indica la similitud ( 0 nada parecido 1 muy
	 *         similares)
	 */
	private double similitud(String p1, String p2) {

		double similitud = 0;
		double similitudParcial = 0;
		int contador = 0;

		String[] words1 = p1.split(" ");
		String[] words2 = p2.split(" ");

		// jaro-winkler

		SimilarityStrategy strategy = new JaroWinklerStrategy();
		StringSimilarityService service = new StringSimilarityServiceImpl(strategy);

		for (int i = 0; i < words1.length; i++) {
			for (int j = 0; j < words2.length; j++) {
				similitudParcial = service.score(words1[i], words2[j]);
				if (similitudParcial > 0) {
					similitud += similitudParcial;
					contador++;
				}
			}
		}

		return similitud / contador;
	}

	/**
	 * Carga los temas, categorias e indicadores en los arrays correspondientes
	 * 
	 * @param br1
	 *            BufferedReader que se encarga de leer el fichero de temas
	 * @param br2
	 *            BufferedReader que se encarga de leer el fichero de categorias
	 * @param br3
	 *            BufferedReader que se encarga de leer el fichero de indicadores
	 * @param categorias
	 *            Array de categorias
	 * @param temas
	 *            Array de temas
	 * @param identificadores
	 *            Array de indicadores
	 * @throws IOException
	 *             control de excepciones
	 */
	private void loadFilesInfo(BufferedReader br2, BufferedReader br3, ArrayList<String> categorias,
			ArrayList<String> identificadores) throws IOException {
		String currentLine2;
		String currentLine3;
		while ((currentLine2 = br2.readLine()) != null) {
			categorias.add(currentLine2);
		}

		while ((currentLine3 = br3.readLine()) != null) {
			identificadores.add(currentLine3);
		}
	}

	// private void loadFilesInfo(BufferedReader br2,ArrayList<String> categorias)
	// throws IOException {
	// String currentLine2;
	// while ((currentLine2 = br2.readLine()) != null) {
	// categorias.add(currentLine2);
	// }
	//
	// System.out.println("Categorias --> "+ categorias.size());
	//
	// }

	/**
	 * Procesa todos los temas y realiza su comparacion con todas las categorias
	 * 
	 * @return true si el proceso fue exitoso, false en caso contrario
	 */
	public boolean procesar() {
		boolean result = false;
		BufferedReader br2, br3 = null;
		FileReader fr2 = null;
		FileReader fr3 = null;
		ArrayList<String> categorias = new ArrayList<String>();
		ArrayList<String> temas = new ArrayList<String>();
		ArrayList<String> identificadores = new ArrayList<String>();
		double[] resultadoParcial;
		double distancia;
		int[] arrayResultados = new int[15];
		int contador;
		String mejorCategoria;
		String[] themeParts;
		boolean esIgual;

		for (int i = 0; i < arrayResultados.length; i++) {
			arrayResultados[i] = 0;
		}

		try {

			temas = this.manageDB.getThemes();

			fr2 = new FileReader(FILENAME2);
			br2 = new BufferedReader(fr2);

			fr3 = new FileReader(FILENAME3);
			br3 = new BufferedReader(fr3);

			loadFilesInfo(br2, br3, categorias, identificadores);

			// compararCategorias(categorias, temas, identificadores, arrayResultados);
			// ArrayList<String> identificadores, int[] arrayResultados) {

			for (String actualTheme : temas) {
				resultadoParcial = new double[categorias.size()];
				contador = 0;
				themeParts = actualTheme.split(" ");
				esIgual = false;
				for (String actualCategory : categorias) {
					for (String individualPart : themeParts) {
						if (!individualPart.toLowerCase().equals("and") && !individualPart.equals("")
								&& !individualPart.equals(",")
								&& (actualCategory.toLowerCase().contains(individualPart.toLowerCase())
										|| actualCategory.toLowerCase().contains(individualPart.toLowerCase() + ","))) {
							arrayResultados[categorias.indexOf(actualCategory)]++;
							esIgual = true;
							update(identificadores.get(temas.indexOf(actualTheme)), "usa_city_datasets_categorized",
									actualCategory);
						}
					}

					if (!esIgual) {
						distancia = similitud(actualTheme, actualCategory);
						resultadoParcial[contador] = distancia;
						contador++;
					}
				}

				if (!esIgual) {
					int mejorResultado = comprobarMejorResultado(resultadoParcial);
					if (resultadoParcial[mejorResultado] >= 0.4) {
						arrayResultados[mejorResultado]++;
						mejorCategoria = selectCategoria(mejorResultado);
						update(identificadores.get(temas.indexOf(actualTheme)), "usa_city_datasets_categorized",
								mejorCategoria);
					} else {
						arrayResultados[arrayResultados.length - 1]++;
						update(identificadores.get(temas.indexOf(actualTheme)), "usa_city_datasets_categorized",
								"Others");
					}
				}

				// System.out.println(temas.indexOf(actualTheme));
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (null != fr2) {
					fr2.close();
					if (null != fr3) {
						fr3.close();
						result = true;
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return result;
	}

	/**
	 * Devuelve la categoria correspondiente al array mejorResultados utilizado en
	 * "procesar" segun su posicionamiento (indice)
	 * 
	 * @param indice
	 * @return
	 */
	private String selectCategoria(int indice) {
		String tema;
		switch (indice) {
		case 0:
			tema = "Administration & Finance";
			break;
		case 1:
			tema = "Business";
			break;
		case 2:
			tema = "Demographics";
			break;
		case 3:
			tema = "Education";
			break;
		case 4:
			tema = "Ethics & Democracy";
			break;
		case 5:
			tema = "Geospatial";
			break;
		case 6:
			tema = "Health";
			break;
		case 7:
			tema = "Recreation & Culture";
			break;
		case 8:
			tema = "Safety";
			break;
		case 9:
			tema = "Services";
			break;
		case 10:
			tema = "Sustainability";
			break;
		case 11:
			tema = "Transport & Infrastructure";
			break;
		case 12:
			tema = "Urban Planning & Housing";
			break;
		case 13:
			tema = "Welfare";
			break;
		default:
			tema = "Others";
			break;
		}

		return tema;
	}

	/**
	 * Actualiza la categoria del tema en base de datos
	 * 
	 * @param id
	 *            identificador del tema
	 * @param tableName
	 *            nombre de la tabla
	 * @param name
	 *            categoria a asignar
	 */
	public void update(String id, String tableName, String name) {
		String sql = "UPDATE usa_city_datasets_categorized SET Category = ? WHERE identifier = ?";

		try {
			PreparedStatement pstmt = manageDB.getConnection()
					.prepareStatement("UPDATE usa_city_datasets_categorized SET Category = ? WHERE identifier = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * Recupera el nombre de las vistas existentes en base de datos
	 */
	public void select() {
		ResultSet result = null;
		try {
			PreparedStatement st = manageDB.getConnection()
					.prepareStatement("select name  from sqlite_master where type = 'view'");
			result = st.executeQuery();
			while (result.next()) {
				System.out.print("name: ");
				System.out.println(result.getString("name"));

				System.out.println("=======================");
			}
		} catch (SQLException ex) {
			System.err.println(ex.getMessage());
		}
	}

	/**
	 * Comprueba la mejor similitud dado un array pasado por parametro
	 * 
	 * @param resultadoParcial
	 *            array con las distintas similitudes
	 * @return
	 */
	private int comprobarMejorResultado(double resultadoParcial[]) {
		double mayor = resultadoParcial[0];
		int indice = 0;

		for (int i = 0; i < resultadoParcial.length; i++) {
			if (resultadoParcial[i] > mayor) {
				indice = i;
				mayor = resultadoParcial[i];
			}
		}

		return indice;

	}

	/**
	 * Redondea un float al numero de decimales pasado por parametro
	 * 
	 * @param d
	 *            float a redondear
	 * @param decimalPlace
	 *            numero de decimales a redondear
	 * @return float redondeado
	 */
	private float round(float d, int decimalPlace) {
		BigDecimal bd = new BigDecimal(Float.toString(d));
		bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
		return bd.floatValue();
	}

	/**
	 * 
	 * @param category
	 * @param value
	 * @param collectValue
	 */
	private void swichtValue(String category, float value, CollectionValue collectValue) {
		value = round(value, 3);

		switch (category) {
		case "Administration & Finance":
			collectValue.setAdministration_finances(value);
			break;
		case "Business":
			collectValue.setBusiness(value);
			break;
		case "Demographics":
			collectValue.setDemographics(value);
			break;
		case "Education":
			collectValue.setEducation(value);
			break;
		case "Ethics & Democracy":
			collectValue.setEthics_democracy(value);
			break;
		case "Geospatial":
			collectValue.setGeospatial(value);
			break;
		case "Health":
			collectValue.setHealth(value);
			break;
		case "Recreation & Culture":
			collectValue.setRecreation_culture(value);
			break;
		case "Safety":
			collectValue.setSafety(value);
			break;
		case "Services":
			collectValue.setServices(value);
			break;
		case "Sustainability":
			collectValue.setSustainability(value);
			break;
		case "Transport & Infrastructure":
			collectValue.setTransport_infrastructure(value);
			break;
		case "Urban Planning & Housing":
			collectValue.setUrban_planning_housing(value);
			break;
		case "Welfare":
			collectValue.setWelfare(value);
			break;
		default:
			break;
		}

	}

	/**
	 * Calcula los valores el json de referencias devuelto por el servicio rest
	 * 
	 * @return objeto de tipo ReferencesValue
	 */
	public FirstMatrixValues calculateFirstMatrix() {

		ResultSet result = null;
		FirstMatrixValues firstMatrix = new FirstMatrixValues();

		try {

			PreparedStatement st1, st2, st3, st4, st5;
			st1 = manageDB.getConnection().prepareStatement("select * from categories_total_references");
			result = st1.executeQuery();

			CollectionValue total_references_collect = new CollectionValue();

			while (result.next()) {
				String category = result.getString("category");
				if (!category.equals("Others")) {
					float value = result.getFloat("total_references");
					swichtValue(category, value, total_references_collect);
				}
			}

			System.out.println("Fin consulta 1");
			firstMatrix.setTotal_references_category_datasets(total_references_collect);

			st2 = manageDB.getConnection().prepareStatement("select * from categories_total_datasets_in");

			result = st2.executeQuery();

			CollectionValue total_datasets_in_category_collect = new CollectionValue();
			while (result.next()) {

				String category = result.getString("CATEGORY");
				if (!category.equals("Others")) {
					float value = result.getFloat("total_datasets_in_category");
					swichtValue(category, value, total_datasets_in_category_collect);
				}

			}

			firstMatrix.setDatasets_references_ornot_github(total_datasets_in_category_collect);
			System.out.println("Fin consulta 2");

			st3 = manageDB.getConnection().prepareStatement("select * from categories_total_datasets_no_referenced");

			CollectionValue categories_total_datasets_no_referenced_collect = new CollectionValue();

			result = st3.executeQuery();
			while (result.next()) {

				String category = result.getString("CATEGORY");
				if (!category.equals("Others")) {
					float value = result.getFloat("total_datasets_no_referenced");
					swichtValue(category, value, categories_total_datasets_no_referenced_collect);
				}

			}
			firstMatrix.setDatasets_not_references_github(categories_total_datasets_no_referenced_collect);
			System.out.println("Fin consulta 3");

			CollectionValue categories_total_datasets_referenced_collect = new CollectionValue();
			st4 = manageDB.getConnection().prepareStatement("select * from categories_total_datasets_referenced");

			result = st4.executeQuery();
			while (result.next()) {

				String category = result.getString("CATEGORY");
				if (!category.equals("Others")) {
					float value = result.getFloat("total_datasets_referenced");
					swichtValue(category, value, categories_total_datasets_referenced_collect);
				}

			}
			firstMatrix.setDatasets_references_github(categories_total_datasets_referenced_collect);
			System.out.println("Fin consulta 4");

			CollectionValue categories_total_repositories_referencing_collect = new CollectionValue();
			st5 = manageDB.getConnection().prepareStatement("select * from categories_total_repositories_referencing");

			result = st5.executeQuery();
			while (result.next()) {
				String category = result.getString("CATEGORY");
				if (!category.equals("Others")) {
					float value = result.getFloat("total_repositories_referencing");
					swichtValue(category, value, categories_total_repositories_referencing_collect);
				}

			}
			firstMatrix
					.setDistinct_repositories_referencing_category(categories_total_repositories_referencing_collect);
			System.out.println("Fin consulta 5");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return firstMatrix;
	}

	/**
	 * Calcula los valores para el json de caracteristicas devuelto en el servicio
	 * rest
	 * 
	 * @return objeto de tipo CharacteristicsValue
	 */
	public SecondMatrixValues calculateSecondMatrix() {

		SecondMatrixValues secondMatrix = new SecondMatrixValues();

		ResultSet result = null;

		try {

			PreparedStatement st1, st2, st3, st4, st5;

			CollectionValue contributors = new CollectionValue();
			st1 = manageDB.getConnection().prepareStatement("select * from categories_contributors");
			result = st1.executeQuery();
			while (result.next()) {

				String category = result.getString("category");
				if (!category.equals("Others")) {
					float value = result.getFloat("contributors");
					swichtValue(category, value, contributors);
				}

			}

			secondMatrix.setContributors(contributors);

			System.out.println("Fin consulta 1");

			CollectionValue contributions = new CollectionValue();
			st2 = manageDB.getConnection().prepareStatement("select * from categories_contributions");

			result = st2.executeQuery();
			while (result.next()) {

				String category = result.getString("category");
				if (!category.equals("Others")) {
					float value = result.getFloat("contributions");
					swichtValue(category, value, contributions);
				}
			}

			secondMatrix.setContributions(contributions);
			System.out.println("Fin consulta 2");

			CollectionValue subscribers = new CollectionValue();
			st3 = manageDB.getConnection().prepareStatement("select * from categories_subscribers");

			result = st3.executeQuery();
			while (result.next()) {

				String category = result.getString("category");
				if (!category.equals("Others")) {
					float value = result.getFloat("subscribers");
					swichtValue(category, value, subscribers);
				}

			}

			secondMatrix.setSubscribers(subscribers);
			System.out.println("Fin consulta 3");

			CollectionValue maturity = new CollectionValue();
			st4 = manageDB.getConnection().prepareStatement("select * from categories_madurity_total");

			result = st4.executeQuery();
			while (result.next()) {

				String category = result.getString("category");
				if (!category.equals("Others")) {
					float value = result.getFloat("madurity_total");
					swichtValue(category, value, maturity);
				}

			}

			secondMatrix.setMaturity(maturity);
			System.out.println("Fin consulta 4");
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return secondMatrix;
	}

}
