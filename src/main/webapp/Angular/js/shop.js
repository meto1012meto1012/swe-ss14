var Shop = angular.module('shop', ['ngResource'])

		.config(function ($routeProvider) {
        $routeProvider.
            when('/artikelA', {templateUrl: 'partials/alleArtikel.html', controller: 'alleArtikelController'}).
            when('/artikelS', {templateUrl: 'partials/artikelSuchen.html', controller: 'artikelSuchenController'}).
            when('/home', {templateUrl: 'partials/home.html'}).
            otherwise({redirectTo: '/home'});
    });


Shop.controller('alleArtikelController', function($scope, ArtikelService){
	
	$scope.artikel = ArtikelService.findAllArtikel(301);
	
});

Shop.controller('artikelSuchenController', function($scope, ArtikelService){
	
});
