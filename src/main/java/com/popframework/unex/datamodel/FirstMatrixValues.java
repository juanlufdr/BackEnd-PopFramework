package com.popframework.unex.datamodel;

public class FirstMatrixValues {
	
	public CollectionValue total_references_category_datasets;
	public CollectionValue datasets_references_ornot_github;
	public CollectionValue datasets_not_references_github;
	public CollectionValue datasets_references_github;
	public CollectionValue distinct_repositories_referencing_category;
	
	public FirstMatrixValues(){

	}

	public CollectionValue getTotal_references_category_datasets() {
		return total_references_category_datasets;
	}

	public void setTotal_references_category_datasets(CollectionValue total_references_category_datasets) {
		this.total_references_category_datasets = total_references_category_datasets;
	}

	public CollectionValue getDatasets_references_ornot_github() {
		return datasets_references_ornot_github;
	}

	public void setDatasets_references_ornot_github(CollectionValue datasets_references_ornot_github) {
		this.datasets_references_ornot_github = datasets_references_ornot_github;
	}

	public CollectionValue getDatasets_not_references_github() {
		return datasets_not_references_github;
	}

	public void setDatasets_not_references_github(CollectionValue datasets_not_references_github) {
		this.datasets_not_references_github = datasets_not_references_github;
	}

	public CollectionValue getDatasets_references_github() {
		return datasets_references_github;
	}

	public void setDatasets_references_github(CollectionValue datasets_references_github) {
		this.datasets_references_github = datasets_references_github;
	}

	public CollectionValue getDistinct_repositories_referencing_category() {
		return distinct_repositories_referencing_category;
	}

	public void setDistinct_repositories_referencing_category(CollectionValue distinct_repositories_referencing_category) {
		this.distinct_repositories_referencing_category = distinct_repositories_referencing_category;
	}

	

}
