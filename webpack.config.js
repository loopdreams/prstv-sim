const path = require('path');

module.exports = {
  entry: [ './target/index.js', './target/main.js'],
  output: {
    path: path.resolve(__dirname, 'resources', 'public', 'js'),
    filename: 'libs.js',
  },
};
