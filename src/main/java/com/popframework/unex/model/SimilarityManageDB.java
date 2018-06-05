package com.popframework.unex.model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.popframework.unex.datamodel.CollectionValue;
import com.popframework.unex.datamodel.SecondMatrixValues;

public class SimilarityManageDB {

	private Connection conn;

	private static SimilarityManageDB mInstance;

	public static SimilarityManageDB getInstance() {
		if (mInstance == null) {
			mInstance = new SimilarityManageDB();
		}

		return mInstance;
	}

	private SimilarityManageDB() {
	}

	public void connect() {

		if (conn == null) {
			try {
				Class.forName("org.sqlite.JDBC");
				String url = "jdbc:sqlite:db/OpenDataCatalogs.sqlite";
				conn = DriverManager.getConnection(url);

				if (conn != null) {
					System.out.println("Connection to SQLite has been established!!.");
				} else {
					System.out.println("Connection to SQLite failed!!.");
				}

			} catch (SQLException e) {
				System.out.println(e.getMessage());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

	public void disconnect() {
		try {
			if (conn != null) {
				conn.close();
				System.out.println("Connection to SQLite has been closed.");
			}
		} catch (SQLException ex) {
			System.out.println(ex.getMessage());
		}
	}

	public Connection getConnection() {
		return conn;
	}

	public void crearVistas() {

		borrarVistas();

		repository_useful_data_for_indicators();
		datasets_categorized_referenced_and_distinct_repository_id_referencing();
		datasets_categorized_not_referenced();
		categories_total_references();
		categories_total_datasets_in();
		categories_total_datasets_no_referenced();
		categories_total_datasets_referenced();
		categories_total_repositories_referencing();
		categories_contributors();
		categories_contributions();
		categories_subscribers();
		categories_madurity_total();

		System.out.println("VISTAS CREADAS");
	}

	public ArrayList<String> getThemes() {
		ArrayList<String> themes = new ArrayList<>();

		ResultSet result = null;

		try {

			PreparedStatement st = this.conn.prepareStatement("select * from usa_city_datasets_categorized");
			result = st.executeQuery();

			while (result.next()) {
				themes.add(result.getString("THEME"));
			}

			System.out.println("Long themes array --> " + themes.size());

		} catch (Exception e) {
			e.printStackTrace();
		}

		return themes;
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
			PreparedStatement pstmt = this.conn
					.prepareStatement("UPDATE usa_city_datasets_categorized SET Category = ? WHERE identifier = ?");
			pstmt.setString(1, name);
			pstmt.setString(2, id);
			pstmt.executeUpdate();
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		}
	}

	public void borrarVistas() {
		ResultSet result = null;
		System.out.println("entra en borrar vistas");

		try {
			PreparedStatement st = conn.prepareStatement("select name from sqlite_master where type = 'view'");
			result = st.executeQuery();
			while (result.next()) {
				String name = result.getString("name");
				System.out.println("Borrando view " + name);
				Statement stmtDelete = conn.createStatement();
				String sqlDelete = "drop view if exists " + name;
				stmtDelete.executeUpdate(sqlDelete);
			}
			System.out.println("VISTAS BORRADAS");
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	private void repository_useful_data_for_indicators() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view repository_useful_data_for_indicators as "
					+ "select distinct repository_id,total_contributors,total_contributions,subscribers_count,created_at,updated_at"
					+ " from measures_repository where  created_at!='' order by repository_id";
			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void datasets_categorized_referenced_and_distinct_repository_id_referencing() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view datasets_categorized_referenced_and_distinct_repository_id_referencing as "
					+ "select distinct c.identifier,c.category,d.repository_id"
					+ " from results_search_open_data as d  join usa_city_datasets_categorized as c on (d.identifier=c.identifier)"
					+ " where repository_id in (select repository_id from repository_useful_data_for_indicators)"
					+ " order by c.category,c.identifier,d.repository_id";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void datasets_categorized_not_referenced() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view datasets_categorized_not_referenced as"
					+ " select distinct identifier,category,NULL as repository_id from usa_city_datasets_categorized"
					+ " where identifier not in"
					+ "(select identifier from datasets_categorized_referenced_and_distinct_repository_id_referencing)"
					+ " order by category,identifier";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_total_references() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_total_references"
					+ " as select category,count (*) as total_references"
					+ " from datasets_categorized_referenced_and_distinct_repository_id_referencing"
					+ " group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_total_datasets_in() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_total_datasets_in as"
					+ " select category,count (distinct identifier) total_datasets_in_category"
					+ " from usa_city_datasets_categorized group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_total_datasets_no_referenced() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_total_datasets_no_referenced as select category,count (distinct identifier) as total_datasets_no_referenced from datasets_categorized_not_referenced group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_total_datasets_referenced() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_total_datasets_referenced as"
					+ " select category, count (distinct identifier) as total_datasets_referenced"
					+ " from datasets_categorized_referenced_and_distinct_repository_id_referencing"
					+ " group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {

			e.printStackTrace();
		}
	}

	private void categories_total_repositories_referencing() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_total_repositories_referencing as"
					+ " select category,count (distinct repository_id) as total_repositories_referencing"
					+ " from datasets_categorized_referenced_and_distinct_repository_id_referencing"
					+ " group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_contributors() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_contributors as"
					+ " select category,sum(total_contributors) as contributors"
					+ " from (select distinct d.repository_id,r.total_contributors,d.category"
					+ " from repository_useful_data_for_indicators as r join"
					+ " datasets_categorized_referenced_and_distinct_repository_id_referencing as d on d.repository_id=r.repository_id"
					+ " order by d.repository_id,r.total_contributors,d.category) group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_contributions() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_contributions as"
					+ " select category, sum(total_contributions) as contributions from"
					+ " (select distinct d.repository_id,r.total_contributions,d.category"
					+ " from repository_useful_data_for_indicators as r join"
					+ " datasets_categorized_referenced_and_distinct_repository_id_referencing as d on d.repository_id=r.repository_id"
					+ " order by d.repository_id,r.total_contributions,d.category) group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_subscribers() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_subscribers as"
					+ " select category, sum(subscribers_count) as subscribers from"
					+ " (select distinct d.repository_id,r.subscribers_count,d.category"
					+ " from repository_useful_data_for_indicators as r join"
					+ " datasets_categorized_referenced_and_distinct_repository_id_referencing as d on d.repository_id=r.repository_id"
					+ " order by d.repository_id,r.subscribers_count,d.category) group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private void categories_madurity_total() {
		try {

			Statement stmtCreate = conn.createStatement();
			String sqlCreate = "create view categories_madurity_total as"
					+ " select category, round(sum( (strftime('%s','2015-11-24 19:19:39 ')-strftime('%s',created_at))/((strftime('%s','2015-11-24 19:19:39 ')-strftime('%s',updated_at))*1.0)),3) as madurity_total"
					+ " from (select distinct d.repository_id,r.created_at,r.updated_at,d.category from repository_useful_data_for_indicators as r join datasets_categorized_referenced_and_distinct_repository_id_referencing as d on d.repository_id=r.repository_id order by d.repository_id,r.created_at,r.updated_at,d.category) group by category order by category";

			stmtCreate.executeUpdate(sqlCreate);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
