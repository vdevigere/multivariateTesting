function TestDataController($scope, $http) {
	$http.jsonp('/tests?callback=JSON_CALLBACK').success(function(data){
		console.log(data);
		$scope.testGroups = data.testGroupList;
	});

	$scope.addTestGroup = function() {
		$scope.testGroups.push({
			"testName" : "",
			"variantList" : [ {
				"variantName" : "",
				"weight" : ""
			}]
		});
	};
};