angular.module('website', []).
    config(function ($routeProvider) {
        $routeProvider.
            when('/artikelA', {templateUrl: 'partials/alleArtikel.html'}).
            when('/artikelS', {templateUrl: 'partials/artikelSuchen.html'}).
            when('/home', {templateUrl: 'partials/home.html'}).
            otherwise({redirectTo: '/home'});
    });