function TestDataController($scope, $http) {
	$http.jsonp('/tests?callback=JSON_CALLBACK').success(function(data){
		console.log(data);
		$scope.testGroups = data.testGroupList;
	});

	$scope.addTestGroup = function(index) {
		$scope.testGroups.splice(index+1, 0, {
			"testName" : "",
			"variantList" : [ {
				"variantName" : "",
				"weight" : ""
			}]
		});
	};
	
	$scope.removeTestGroup = function(index){
		$scope.testGroups.splice(index, 1);
	};
	
	$scope.addVariant = function(parent, index){
		$scope.testGroups[parent].variantList.splice(index+1, 0,{"variantName":"", "weight":""});
	}

	$scope.removeVariant = function(parent, index){
		$scope.testGroups[parent].variantList.splice(index, 1);
	}
};