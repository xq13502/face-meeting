<template>
    <div class="face-login-container">
        <div class="face-login-box">
            <h3 class="title">人脸识别登录</h3>

            <div class="face-content">
                <div class="camera-container" v-if="showCamera">
                    <video id="video" width="320" height="240" autoplay></video>
                    <canvas id="canvas" width="320" height="240" style="display: none;"></canvas>
                    <p class="tips">{{ faceLoginTips }}</p>
                </div>
                <el-button v-else type="primary" @click="startFaceLogin" :loading="loading"
                    style="width: 320px; margin-bottom: 20px;">
                    开始人脸识别
                </el-button>
            </div>

            <div class="action-buttons">
                <router-link to="/login">
                    <el-button style="width: 320px;" plain>返回账号登录</el-button>
                </router-link>
            </div>
        </div>
    </div>
</template>

<script>
    export default {
        name: "FaceLogin",
        data() {
            return {
                showCamera: false,
                faceLoginTips: '请正视摄像头',
                mediaStreamTrack: null,
                loading: false,
                redirect: undefined
            }
        },
        watch: {
            $route: {
                handler: function (route) {
                    this.redirect = route.query && route.query.redirect;
                },
                immediate: true
            }
        },
        beforeDestroy() {
            this.closeCamera();
        },
        methods: {
            // 开始人脸登录
            async startFaceLogin() {
                if (this.loading) return;

                try {
                    this.showCamera = true;
                    this.faceLoginTips = '正在打开摄像头...';
                    await this.$nextTick();
                    await this.openCamera();
                    this.faceLoginTips = '请正视摄像头，3秒后自动识别';
                } catch (error) {
                    this.$message.error('摄像头启动失败，请检查设备权限');
                    this.showCamera = false;
                    this.faceLoginTips = '摄像头启动失败';
                }
            },

            // 打开摄像头
            async openCamera() {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({
                        video: { width: 320, height: 240 },
                        audio: false
                    });

                    const video = document.getElementById('video');
                    this.mediaStreamTrack = stream.getTracks()[0];
                    video.srcObject = stream;
                    await video.play();

                    // 3秒后自动拍照
                    setTimeout(() => {
                        this.takeFacePhoto();
                    }, 3000);
                } catch (error) {
                    console.error('摄像头启动失败:', error);
                    throw error;
                }
            },

            // 拍照并进行人脸登录
            async takeFacePhoto() {
                if (this.loading) return;

                try {
                    const video = document.getElementById('video');
                    const canvas = document.getElementById('canvas');
                    const ctx = canvas.getContext('2d');

                    // 绘制视频帧到canvas
                    ctx.drawImage(video, 0, 0, 320, 240);

                    // 获取图像数据
                    const faceImage = canvas.toDataURL();

                    // 关闭摄像头
                    this.closeCamera();

                    // 执行人脸登录
                    this.loading = true;
                    this.faceLoginTips = '正在识别中...';

                    // 先进行人脸登录
                    await this.$store.dispatch('FaceLogin', { faceImage: faceImage });

                    // // 登录成功后获取用户信息
                    await this.$store.dispatch('GetInfo');

                    // 3. 生成路由
                    const accessRoutes = await this.$store.dispatch('GenerateRoutes');

                    // 4. 动态添加路由
                    this.$router.addRoutes(accessRoutes);

                    this.$message.success('登录成功');
                    this.$router.push({ path: this.redirect || "/" });
                } catch (error) {
                    console.error('登录失败:', error);
                    this.$message.error(error.message || '人脸识别失败，请重试');
                    this.showCamera = false;
                    this.loading = false;
                    this.faceLoginTips = '识别失败，请重试';
                } finally {
                    this.loading = false;
                }
            },

            // 关闭摄像头
            closeCamera() {
                if (this.mediaStreamTrack) {
                    this.mediaStreamTrack.stop();
                    this.mediaStreamTrack = null;
                }
                this.showCamera = false;
            }
        }
    }
</script>

<style lang="scss" scoped>
    .face-login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        background-image: url("../assets/images/bj2.png");
        background-size: cover;
    }

    .face-login-box {
        width: 400px;
        padding: 30px;
        border-radius: 8px;
        background-color: rgba(255, 255, 255, 0.2);
        box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);

        .title {
            margin: 0 0 30px;
            text-align: center;
            color: white;
            font-size: 24px;
        }

        .face-content {
            display: flex;
            flex-direction: column;
            align-items: center;
            margin-bottom: 20px;

            .camera-container {
                text-align: center;

                video {
                    border-radius: 8px;
                    border: 2px solid rgba(255, 255, 255, 0.3);
                    background-color: #000;
                }

                .tips {
                    margin-top: 15px;
                    color: #fff;
                    font-size: 14px;
                }
            }
        }

        .action-buttons {
            text-align: center;
            margin-top: 20px;
        }
    }

    .scanning {
        position: relative;

        &::after {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            right: 0;
            height: 2px;
            background: rgba(0, 255, 0, 0.5);
            animation: scanning 2s linear infinite;
        }
    }

    @keyframes scanning {
        0% {
            transform: translateY(0);
        }

        100% {
            transform: translateY(240px);
        }
    }
</style>