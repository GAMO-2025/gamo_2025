pipeline {
    agent any

    parameters {
        string(name: 'DEPLOY_USER', defaultValue: 'pjhyun0225', description: 'SSH 접속 사용자명')
        string(name: 'DEPLOY_HOST', defaultValue: '34.64.202.182', description: '배포 대상 서버 IP')
        string(name: 'DEPLOY_PATH', defaultValue: '/home/pjhyun0225/gamo_service', description: '프로젝트 배포 경로')
        string(name: 'CREDENTIALS_ID', defaultValue: 'service-server-ssh', description: 'SSH 자격 증명 ID')
    }

    stages {
        stage('Checkout') {
            steps {
                echo "GitHub main 브랜치 코드 체크아웃 중..."
                git branch: 'main', credentialsId: 'github-clone', url: 'https://github.com/GAMO-2025/gamo_2025.git'
            }
        }

        stage('Inject Secrets and Deploy') {
            steps {
                withCredentials([
                    file(credentialsId: 'application-secret', variable: 'APP_SECRET_FILE'),
                    file(credentialsId: 'google-service-account', variable: 'GOOGLE_KEY_FILE')
                ]) {
                    echo "서버(${params.DEPLOY_HOST})에 SSH 접속하여 Spring Boot 애플리케이션 배포 시작..."
                    sshagent([params.CREDENTIALS_ID]) {
                        sh """
                            echo '[1] 시크릿 파일 전송 중...'
                            scp -o StrictHostKeyChecking=no $APP_SECRET_FILE ${params.DEPLOY_USER}@${params.DEPLOY_HOST}:${params.DEPLOY_PATH}/src/main/resources/application-secret.properties
                            scp -o StrictHostKeyChecking=no $GOOGLE_KEY_FILE ${params.DEPLOY_USER}@${params.DEPLOY_HOST}:${params.DEPLOY_PATH}/src/main/resources/google-service-account.json

                            echo '[2] 원격 서버에서 빌드 및 배포 실행...'
                            ssh -o StrictHostKeyChecking=no ${params.DEPLOY_USER}@${params.DEPLOY_HOST} << 'EOF'
                                cd ${params.DEPLOY_PATH} || exit 1

                                echo '[2-1] 최신 main 코드 반영 중...'
                                git fetch origin main &&
                                git reset --hard origin/main

                                echo '[2-2] Gradle 빌드 실행 중...'
                                chmod +x ./gradlew
                                ./gradlew clean build

                                JAR_NAME="web-0.0.1-SNAPSHOT.jar"
                                PID=\$(pgrep -f "\$JAR_NAME")
                                if [ -n "\$PID" ]; then
                                    echo "기존 프로세스(\$PID) 종료 중..."
                                    kill -9 \$PID
                                fi

                                echo "새 애플리케이션 실행 중..."
                                nohup java -jar "./build/libs/\$JAR_NAME" > app.log 2>&1 &
EOF
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo 'Spring Boot 서비스가 정상적으로 재배포되었습니다.'
        }
        failure {
            echo '배포 실패. Jenkins 콘솔 로그를 확인하세요.'
        }
    }
}
