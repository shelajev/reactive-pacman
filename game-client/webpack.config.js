const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const webpack = require('webpack');
const CopyPlugin = require('copy-webpack-plugin');

module.exports = {
    entry: './src/main/typescript/boot.ts',
    mode: 'development',
    devtool: 'source-map',
    devServer: {
        contentBase: path.join(__dirname, 'build'),
        compress: true,
        host: '192.168.10.105',
        port: 9000,
        hot: true
    },
    plugins: [
        new HtmlWebpackPlugin({
            title: 'Hot Module Replacement',
            template: path.join(__dirname, 'src/main/resources/public/index.html')
        }),
        new CopyPlugin([
            { from: 'src/main/resources/public', to: './' },
        ]),
        new webpack.HotModuleReplacementPlugin()
    ],
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                use: 'ts-loader',
                exclude: /node_modules/
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
        publicPath: path.resolve(__dirname, 'src/main/resources/public'),
        path: path.resolve(__dirname, 'build')
    }
};