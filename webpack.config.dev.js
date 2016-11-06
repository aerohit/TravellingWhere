var path = require('path');
var webpack = require('webpack');

var config = {
  devtool: 'cheap-module-eval-source-map',
  entry: [
    './js/app.js'
  ],
  output: {
    path: path.join(__dirname, 'public/javascripts/compiled'),
    filename: 'bundle.js',
    publicPath: '/public/javascripts/compiled/'
  },
  plugins: [
    new webpack.NoErrorsPlugin()
  ],
  module: {
    loaders: [
      {
        test: /\.js$/,
        loaders: ['babel'],
        exclude: /node_modules/
      }
    ]
  }
};

module.exports = config;
