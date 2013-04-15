function TestDataController($scope, $http) {
	$http.get('/tests?callback=dummy').success(function(data) {
		$scope.testGroups = data.testGroupList;
	});

	$scope.addTestGroup = function() {
		$scope.testGroups = [ {
			"testName" : "",
			"variantList" : [ {
				"variantName" : "",
				"weight" : ""
			}]
		} ];
	};
};