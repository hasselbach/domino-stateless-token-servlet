function Hello($scope, $http) {
	$http.defaults.headers.common['X-AUTH-TOKEN'] = 'MTQyNDI2OTE0NDgxNCFUVGVzdFVzZXIwMQ==!HyC1mnvvdaneLaW0Wn48kZ1MaTrdowr1e4nWBRWRX8Y=';
    $http.get('http://localhost/token/validate/').
        success(function(data) {
            $scope.greeting = data;
        });
        
}