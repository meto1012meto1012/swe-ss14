var Shop = angular.module('shop', ['ngResource'])

		.config(function ($routeProvider) {
        $routeProvider.
            when('/artikelA', {templateUrl: 'partials/alleArtikel.html', controller: 'alleArtikelController'}).
            when('/artikelS', {templateUrl: 'partials/artikelSuchen.html', controller: 'artikelAnlegenController'}).
            when('/artikelU', {templateUrl: 'partials/Artikelaendern.html', controller: 'artikelAendernController'}).
			when('/home', {templateUrl: 'partials/home.html'});
    });


Shop.controller('alleArtikelController', ['$scope', 'ArtikelService', '$location',
	function($scope, ArtikelService, $location){
	
	
	$scope.artikel = ArtikelService.findAllArtikel(301);

	$scope.editArtikel = function (artikelid) {
            $location.path('/artikelU/').search('id', artikelid);
        };
}]);

Shop.controller('artikelAnlegenController', ['$scope', 'ArtikelFactory', '$location',
    function ($scope, ArtikelFactory, $location) {

        // callback for ng-click 'createNewUser':
        $scope.createNewArtikel = function () {
            ArtikelFactory.create($scope.artikel);
            $location.path('/artikelU');
        };
    }]);

Shop.controller('artikelAendernController', ['$scope', '$routeParams', 'ArtikelFactory', 'ArtikelService', '$location',
    function ($scope, $routeParams, ArtikelFactory, ArtikelService, $location) {

        // callback for ng-click 'updateUser':
        $scope.updateArtikel = function () {
			alert('update7');
			
            var artikel = ArtikelService.findAllArtikel($routeParams.id);
            $id = artikel.id;
			artikel.bezeichnung = "ksdjf";
			artikel.preis = "2";
			
			ArtikelFactory.update({id:$id}, artikel);
			$location.path('/home');
        };

        // callback for ng-click 'cancel':
        $scope.cancel = function () {
            $location.path('/home');
        };

		alert('test3');
		alert('param: ' + $routeParams.id);
        //$scope.artikel = ArtikelFactory.show({id: $routeParams.id});
    }]);