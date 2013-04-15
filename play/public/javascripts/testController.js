function TestDataController($scope, $http){
	$http.get('http://localhost:9000/data/tests').
  		success(function(data) {
  			$scope.testGroups = data;
  });	
};