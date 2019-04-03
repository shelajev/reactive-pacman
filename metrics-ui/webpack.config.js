const path = require('path');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
  entry: './src/index.js',
  mode: 'development',
  devtool: 'source-map',
  devServer: {
      contentBase: path.join(__dirname, 'build'),
      compress: true,
      port: 4444,
      hot: true
  },
  plugins: [
    new CopyPlugin([
      { from: 'src/index.html', to: './' },
    ]),
  ],
  resolve: {
    extensions: ['.js']
  },
  output: {
    filename: 'index.js',
    path: path.resolve(__dirname, 'build')
  }
};