const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  productionSourceMap: false,
  configureWebpack: {
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          vendor: {
            name: 'vendor',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial'
          },
          common: {
            name: 'common',
            minChunks: 2,
            priority: 5,
            reuseExistingChunk: true
          }
        }
      }
    }
  },
  css: {
    extract: true,
    sourceMap: false
  },
  devServer: {
    port: 3000,
    open: true,
    compress: true
  },
  chainWebpack: config => {
    config.module
      .rule('images')
      .use('image-webpack-loader')
      .loader('image-webpack-loader')
      .options({
        mozjpeg: { progressive: true, quality: 65 },
        optipng: { enabled: false },
        pngquant: { quality: [0.65, 0.90], speed: 4 },
        gifsicle: { interlaced: false }
      })
      .end()
    
    config.plugins.delete('preload')
    config.plugins.delete('prefetch')
    
    config
      .plugin('compression')
      .use(require('compression-webpack-plugin'), [{
        test: /\.(js|css|html|svg)$/,
        threshold: 10240,
        minRatio: 0.8
      }])
  }
})
