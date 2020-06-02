const path = require('path');
const CompressionPlugin = require('compression-webpack-plugin');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: './src/main/typescript/boot.ts',
    mode: 'production',
    devServer: {
        contentBase: path.join(__dirname, 'build'),
        compress: true,
        host: "0.0.0.0",
        port: 9010,
        hot: true
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Multiplayer Pac-Man on RSocket',
            template: path.join(__dirname, 'src/main/resources/public/index.html'),
            favicon: path.join(__dirname, 'src/main/resources/public/favicon.ico')
        }),
        new CopyPlugin({
            patterns: [
                { from: 'src/main/resources/public', to: './' },
            ]
        }),
        new CompressionPlugin(),
        new webpack.HotModuleReplacementPlugin()
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: [
                    /node_modules/,
                ]
            },
            {
                test: /\.(png|svg|jpg|gif)$/,
                use: [
                    'file-loader'
                ]
            }
        ]
    },
    resolve: {
        extensions: ['.tsx', '.ts', '.js']
    },
    output: {
        filename: 'bundle.js',
        path: path.resolve(__dirname, 'build')
    }
};