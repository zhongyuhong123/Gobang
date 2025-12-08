const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 生产环境配置
  productionSourceMap: false,
  // 构建优化配置
  configureWebpack: {
    // 代码分割
    optimization: {
      splitChunks: {
        chunks: 'all',
        cacheGroups: {
          // 第三方依赖
          vendor: {
            name: 'vendor',
            test: /[\\/]node_modules[\\/]/,
            priority: 10,
            chunks: 'initial' // 只打包初始时依赖的第三方
          },
          // 公共组件
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
  // CSS优化
  css: {
    extract: true,
    sourceMap: false,
    loaderOptions: {
      css: {
        // 压缩CSS
        minimize: true
      }
    }
  },
  // 开发服务器配置
  devServer: {
    port: 8081,
    open: true,
    compress: true
  },
  // 资源优化
  chainWebpack: config => {
    // 图片优化
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
    
    // 移除预加载和预取
    config.plugins.delete('preload')
    config.plugins.delete('prefetch')
    
    // 启用gzip压缩
    config.plugins
      .tap(args => {
        // 添加gzip压缩插件
        const CompressionPlugin = require('compression-webpack-plugin')
        args.push(
          new CompressionPlugin({
            test: /\.(js|css|html|svg)$/,
            threshold: 10240,
            minRatio: 0.8
          })
        )
        return args
      })
  }
})
