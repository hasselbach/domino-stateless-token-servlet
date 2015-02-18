function Hello($scope, $http) {
    $http.get('http://localhost/token/static/greeting.json').
        success(function(data) {
            $scope.greeting = data;
        });
}