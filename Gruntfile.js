module.exports = function(grunt) {
  grunt.initConfig({
    clean: {
      dist: {
        src: [ 'public/css', 'public/js', 'src/assets/temp', 'public/fonts' ]
      }
    },
    watch: {
      scripts: {
        files: ['src/app/*/*.*', 'src/app/*/*/*.*', 'src/app/*.*'],
        tasks: ['build'],
        options: {
          spawn: false
        }
      }
    },
    copy: {
      font_awesome: {expand: true, cwd: 'node_modules/font-awesome/fonts/', src: '*', dest: 'public/fonts/'},
      font_bs_glyphicons: {expand: true, cwd: 'node_modules/bootstrap/fonts/', src: '*', dest: 'public/fonts/'},
      ace_theme: {expand: true, cwd: 'src/assets/js/ace', src: '*', dest: 'public/js/ace'},
      ace_scripts: {expand: true, cwd: 'node_modules/ace-builds/src-min/', src: ['mode-json.js', 'worker-json.js'], dest: 'public/js/ace'}
    },
    less: {
        bootstrap: {
            files: {
                'src/assets/temp/css/bootstrap.css': 'src/assets/less/bootstrap-build.less'
            }
        },
    },
    concat: {
      vendor_js: {
        src: [
          'node_modules/jquery/dist/jquery.js',
          'node_modules/angular/angular.js',
          'node_modules/angular-animate/angular-animate.js',
          'node_modules/angular-route/angular-route.js',
          'node_modules/bootstrap/dist/js/bootstrap.js',
          'src/assets/libs/jsontree/jsontree.min.js',
          'node_modules/angular-ui-bootstrap/dist/ui-bootstrap.js',
          'node_modules/ace-builds/src-min/ace.js',
        ],
        dest: 'src/assets/temp/js/lib.js'
      },
      app_js: {
        src: [
          'src/app/app.routes.js',
          'src/app/components/*/*.js',
          'src/app/shared/*.js',
          'src/app/shared/*/*.js'
        ],
        dest: 'src/assets/temp/js/app.js'
      },
    },
    cssmin: {
        bootstrap: {
            expand: false,
            files: {'public/css/lib.css': ['src/assets/temp/css/bootstrap.css', 'node_modules/font-awesome/css/font-awesome.css']}
        },
        app: {
            expand: false,
            files: {'public/css/app.css': ['src/app/css/app.css']}
        }
    },
    terser: {
      vendor_js:{
        sourceMap: false,
        files: {
          'public/js/lib.js':'src/assets/temp/js/lib.js'
        }
      },
      app_js: {
        options: {
          mangle: false,
        },
        sourceMap: false,
        files: {
          'public/js/app.js':'src/assets/temp/js/app.js',
        }
      }
    },
    jshint: {
      cerebro: {
        src: ['src/app/components/*/*.js', 'src/app/shared/*.js', 'src/app/shared/*/*.js']
      }
    },
    qunit: {
      all: ['./tests/all.html']
    },
    karma: {
      unit: {configFile: 'tests/karma.config.js', keepalive: true}
    },
    eslint: {
      target: ['src/app/app.routes.js', 'src/app/components/*/*.js', 'src/app/shared/*.js', 'src/app/shared/*/*.js'],
      options: {
        configFile: 'conf/eslint.json',
        failOnError: false,
      },
    }
  });
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-connect');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-contrib-cssmin');
  grunt.loadNpmTasks('grunt-contrib-jshint');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-qunit');
  grunt.loadNpmTasks('grunt-contrib-watch');
  grunt.loadNpmTasks('grunt-eslint');
  grunt.loadNpmTasks('grunt-karma');
  grunt.loadNpmTasks('grunt-terser');

  grunt.registerTask('dev', ['watch'])
  grunt.registerTask('build', ['clean', 'jshint', 'eslint', 'less', 'concat', 'copy', 'cssmin', 'terser', 'qunit']);
  grunt.registerTask('test', ['build', 'karma'])
};
