pipeline {
  agent any
  tools { maven 'maven3' }

  stages {

    stage('拉取代码') {
      steps { checkout scm }
    }

    stage('编译打包') {
      steps { sh 'mvn clean package -DskipTests' }
    }

    stage('复制到运行目录') {
      steps {
        sh '''
          cp target/*.jar /app/rag-backend.jar
        '''
      }
    }
  }
}
