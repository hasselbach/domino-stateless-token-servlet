function Hello($scope, $http) {
	
	// add a valid token to the headers
	$http.defaults.headers.common['X-AUTH-TOKEN'] = 'MTQyNDI2OTE0NDgxNCFUVGVzdFVzZXIwMQ==!HyC1mnvvdaneLaW0Wn48kZ1MaTrdowr1e4nWBRWRX8Y=';

	// load the data
    $http.get('http://localhost/token/validate/').
        success(function(data) {
            $scope.greeting = data;
        });
        
}