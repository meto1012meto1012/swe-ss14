/* 
 * Copyright (C) 2014 Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
myApp = angular.module("rest", []);

myApp.controller('ArtikelCtrl', ['$scope', '$http', function ($scope, $http) {
 
  // create a user Object
  $scope.artikel = {};
 
  // Initiate a model as an empty string
  $scope.artikel.bezeichnung = '';
 
  // We want to make a call and get
  // the person's username
  $http({
    method: 'GET',
    url: 'https://localhost:8443/shop/rest/artikel/301/'
  })
  .success(function (data, status, headers, config) {
    // See here, we are now assigning this username
    // to our existing model!
    $scope.artikel.bezeichnung = data.artikel.bezeichnung;
  })
  .error(function (data, status, headers, config) {
    // something went wrong :(
  });
}]);

