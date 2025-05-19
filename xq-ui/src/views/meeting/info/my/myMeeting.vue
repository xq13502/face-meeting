<template>
    <div class="app-container">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch"
            label-width="68px">
            <el-form-item label="会议名称" prop="meetingName">
                <el-input v-model="queryParams.meetingName" placeholder="请输入会议名称" clearable
                    @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="发起人" prop="userName">
                <el-input v-model="queryParams.userName" placeholder="请输入发起人姓名" clearable
                    @keyup.enter.native="handleQuery" />
            </el-form-item>
            <el-form-item label="会议状态" prop="status">
                <el-select v-model="queryParams.status" clearable placeholder="请选择状态">
                    <el-option v-for="item in optionStatus" :key="item.value" :label="item.label" :value="item.value"
                        :disabled="item.status == 1"></el-option>
                </el-select>
            </el-form-item>


            <el-form-item label="会议日期" prop="timeRange">
                <el-date-picker style="width: 350px;" v-model="timeRange" type="datetimerange" range-separator="至"
                    start-placeholder="开始时间" end-placeholder="结束时间" value-format="yyyy-MM-dd HH:mm:ss"
                    @change="handleTimeRangeChange" :picker-options="pickerOptions"></el-date-picker>
            </el-form-item>
            <el-form-item>
                <el-button type="primary" icon="el-icon-search" size="mini" round @click="handleQuery">查询</el-button>
                <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
            </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
            <el-col :span="1.5">
                <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
                    v-hasPermi="['meeting:info:export']">导出</el-button>
            </el-col>
        </el-row>

        <el-table v-loading="loading" :data="meetingList" @selection-change="handleSelectionChange">
            <el-table-column type="selection" width="55" align="center" />
            <!-- <el-table-column label="会议编号" align="center" prop="meetingId" /> -->
            <el-table-column label="序号" align="center" width="120">
                <template slot-scope="scope">
                    {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
                </template>
            </el-table-column>
            <el-table-column label="会议名称" align="center" show-overflow-tooltip prop="meetingName" />
            <el-table-column label="会议描述" align="center" show-overflow-tooltip prop="meetingDesc" />
            <!-- <el-table-column label="发起人编号" align="center" prop="userId" /> -->
            <el-table-column label="发起人" align="center" prop="userName" />
            <el-table-column label="会议类型" align="center" prop="type">
                <template slot-scope="scope">
                    <!-- 根据状态值渲染不同标签 -->
                    <el-tag :type="getMeetingType(scope.row.type)" size="small">
                        {{ getMeetingLabel(scope.row.type) }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column label="所属会议室" align="center" prop="meetingRooms" width="90">
                <template slot-scope="scope">
                    <!-- 自定义列内容，点击时查询设备信息 -->
                    <el-button type="text" @click="handleQueryRooms(scope.row)">查看会议室</el-button>
                </template>
            </el-table-column>
            <el-table-column label="该会议签到情况" align="center" prop="memberNumber" width="115">
                <template slot-scope="scope">
                    <!-- 自定义列内容，点击时查询设备信息 -->
                    <el-button type="text" @click="handleQueryPersonal(scope.row)">该会议成员</el-button>
                </template>
            </el-table-column>
            <el-table-column label="开始时间" align="center" prop="startTime" width="150">
                <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.startTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="结束时间" align="center" prop="endTime" width="150">
                <template slot-scope="scope">
                    <span>{{ parseTime(scope.row.endTime) }}</span>
                </template>
            </el-table-column>
            <el-table-column label="会议状态" align="center" prop="status">
                <template slot-scope="scope">
                    <!-- 根据状态值渲染不同标签 -->
                    <el-tag :type="getStatusType(scope.row.status)" size="small">
                        {{ getStatusLabel(scope.row.status) }}
                    </el-tag>
                </template>
            </el-table-column>


            <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
                <template slot-scope="scope">
                    <el-button size="mini" type="text" @click="handleCheckIn(scope.row)"
                        v-hasPermi="['meeting:info:check:in']">签到</el-button>
                    <el-button size="mini" type="text" @click="handleCheckOut(scope.row)"
                        v-hasPermi="['meeting:info:check:out']">签退</el-button>
                    <el-button size="mini" type="text" @click="handleLeave(scope.row)"
                        v-hasPermi="['meeting:info:leave']">请假</el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
            @pagination="getUserMeetingList" />


        <!-- 会议室设备详情弹窗 -->
        <el-dialog title="会议室详情" :visible.sync="dialogVisible" width="50%" style="margin-top: 100px;"
            :modal-append-to-body="false" :body-style="{ maxHeight: '60vh', overflowY: 'auto' }">
            <!-- 设备表格 -->
            <el-table :data="queryResult" empty-text="线上会议无实质会议室" border style="width: 100%">
                <el-table-column prop="roomName" label="会议室名称" align="center" />
                <el-table-column prop="location" label="位置信息" align="center" />
                <el-table-column label="容纳人数" prop="capacity" align="center">
                </el-table-column>
                <el-table-column prop="createTime" label="建立时间" align="center" />
            </el-table>

            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisible = false">关闭</el-button>
            </span>
        </el-dialog>

        <!-- 参会人员列表详情弹窗 -->
        <el-dialog title="参会人员签到情况" :visible.sync="dialogVisibleUserList" width="60%" style="margin-top: 50px;"
            :modal-append-to-body="false" :body-style="{ maxHeight: '60vh', overflowY: 'auto' }">
            <!-- 简化步骤条 -->
            <div v-if="currentMeetingStatus === '2'"
                style="text-align: center; margin-bottom: 20px; padding: 10px; background-color: #FEF0F0; border-radius: 4px;">
                <i class="el-icon-error"
                    style="color: #F56C6C; font-size: 24px; vertical-align: middle; margin-right: 8px;"></i>
                <span style="color: #F56C6C; font-size: 16px; font-weight: bold;">审批未通过</span>
            </div>
            <el-steps v-else :active="getActiveStep(currentMeetingStatus)" finish-status="success" align-center
                style="margin-bottom: 20px">
                <el-step title="审批中" description="等待审批"></el-step>
                <el-step title="已通过" description="审批通过"></el-step>
                <el-step title="已开始" description="会议进行中"></el-step>
                <el-step title="已结束" description="会议结束"></el-step>
            </el-steps>

            <!-- 设备表格 -->
            <el-table :data="queryUserResult" empty-text="暂无参会人员签到数据" border style="width: 100%">
                <el-table-column prop="userName" label="姓名" align="center" />
                <el-table-column prop="nickName" label="称谓" align="center" />
                <el-table-column label="邮箱" prop="email" align="center" width="200">
                </el-table-column>
                <el-table-column prop="phonenumber" label="电话号码" align="center" width="110" />
                <el-table-column prop="cmsAttendance.signInStatus" label="签到状态" align="center">
                    <template slot-scope="scope">
                        <!-- 根据状态值渲染不同标签 -->
                        <el-tag :type="getSignInStatusType(scope.row.cmsAttendance.signInStatus)" size="small">
                            {{ getSignInStatusLabel(scope.row.cmsAttendance.signInStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="cmsAttendance.signOutStatus" label="签退状态" align="center">
                    <template slot-scope="scope">
                        <!-- 根据状态值渲染不同标签 -->
                        <el-tag :type="getSignOutStatusType(scope.row.cmsAttendance.signOutStatus)" size="small">
                            {{ getSignOutStatusLabel(scope.row.cmsAttendance.signOutStatus) }}
                        </el-tag>
                    </template>
                </el-table-column>

                <el-table-column label="请假状态" align="center" prop="cmsAttendance.isLeave">
                    <template slot-scope="scope">
                        <!-- 根据状态值渲染不同标签 -->
                        <el-tag :type="getIsLeaveStatusType(scope.row.cmsAttendance.isLeave)" size="small">
                            {{ getIsLeaveStatusLabel(scope.row.cmsAttendance.isLeave) }}
                        </el-tag>
                    </template>
                </el-table-column>
            </el-table>

            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisibleUserList = false">关闭</el-button>
            </span>
        </el-dialog>

        <!-- 修改后的签到弹窗模板 -->
        <el-dialog :title="checkDialogTitle" :visible.sync="dialogCheckVisible" width="550px"
            style="margin-top: 100px;">
            <div class="status-container">
                <!-- 成功状态 -->
                <div v-if="signStatus === 'success'" class="success-status">
                    <i class="el-icon-success"></i>
                    <p>{{ checkActionType === 'checkIn' ? '签到成功' : '签退成功' }}</p>
                </div>

                <!-- 失败状态 -->
                <div v-else-if="signStatus === 'error'" class="error-status">
                    <i class="el-icon-error"></i>
                    <p>{{ checkActionType === 'checkIn' ? '签到失败，请重试' : '签退失败，请重试' }}</p>
                </div>

                <!-- 加载状态 -->
                <div v-else-if="signStatus === 'loading'" class="loading-status">
                    <i class="el-icon-loading"></i>
                    <p>{{ checkActionType === 'checkIn' ? '签到处理中，请稍候...' : '签退处理中，请稍候...' }}</p>
                </div>

                <!-- 已处理/已完成状态 -->
                <div v-else-if="signStatus === 'already_done'" class="done-status">
                    <i class="el-icon-warning-outline"></i>
                    <p>{{ checkActionType === 'checkIn' ? '已签到，请勿重复操作' : '已签退，请勿重复操作' }}</p>
                </div>


                <!-- 请假状态 -->
                <div v-else-if="signStatus === 'leave'" class="leave-status">
                    <i class="el-icon-moon-night"></i>
                    <p>您已请假，无法进行签到操作...</p>
                </div>

                <!-- 需要先签到提示 -->
                <div v-else-if="signStatus === 'need_check_in'" class="need-checkin-status">
                    <i class="el-icon-warning"></i>
                    <p>请先完成签到...</p>
                </div>

                <template v-else>
                    <!-- 初始选择界面 -->
                    <div class="check-options" v-if="!showFaceRecognition && !showQrCode && !showManualCheck">
                        <el-row :gutter="20">
                            <el-col :span="8">
                                <el-button type="primary" @click="handleFaceRecognition" round>
                                    人脸{{ checkActionType === 'checkIn' ? '签到' : '签退' }}
                                </el-button>
                            </el-col>
                            <el-col :span="8">
                                <el-button type="primary" @click="handleQrCodeCheck" round>
                                    二维码{{ checkActionType === 'checkIn' ? '签到' : '签退' }}
                                </el-button>
                            </el-col>
                            <el-col :span="8">
                                <el-button type="primary" @click="handleManualCheck" round>
                                    手动{{ checkActionType === 'checkIn' ? '签到' : '签退' }}
                                </el-button>
                            </el-col>
                        </el-row>
                    </div>

                    <!-- 人脸识别区域 -->
                    <div v-if="showFaceRecognition" class="check-container">
                        <div class="back-button">
                            <el-button type="text" icon="el-icon-back" @click="resetCheckMethod">返回选择其他方式</el-button>
                        </div>
                        <div class="face-recognition-container">
                            <video id="video" width="400px" height="300px" autoplay="autoplay"></video>
                            <canvas id="canvas" width="400px" height="300px" style="display: none;"></canvas>
                            <p class="tip-text">{{ faceRecognitionTips }}</p>
                        </div>
                    </div>

                    <!-- 二维码签到区域 -->
                    <div v-if="showQrCode" class="check-container">
                        <div class="back-button">
                            <el-button type="text" icon="el-icon-back" @click="resetCheckMethod">返回选择其他方式</el-button>
                        </div>
                        <div class="qr-code-container">
                            <div class="qr-code-wrapper">
                                <canvas ref="qrCanvas"></canvas>
                            </div>
                            <p class="tip-text">请使用微信扫描完成{{ checkActionType === 'checkIn' ? '签到' : '签退' }}</p>
                            <p class="pending-tip" v-if="signStatus === ''">等待扫码中...</p>
                        </div>
                    </div>

                    <!-- 手动签到区域 -->
                    <div v-if="showManualCheck" class="check-container">
                        <div class="back-button">
                            <el-button type="text" icon="el-icon-back" @click="resetCheckMethod">返回选择其他方式</el-button>
                        </div>
                        <div class="manual-check">
                            <el-button type="primary" @click="handleConfirmCheck" round>
                                确认{{ checkActionType === 'checkIn' ? '签到' : '签退' }}
                            </el-button>
                            <p class="tip-text">请在会议室现场完成{{ checkActionType === 'checkIn' ? '签到' : '签退' }}</p>
                        </div>
                    </div>
                </template>
            </div>
        </el-dialog>
        <!-- 新增 请假弹窗 -->
        <el-dialog title="会议请假" :visible.sync="dialogLeaveVisible" width="500px" style="margin-top: 100px;">
            <el-form :model="leaveForm" :rules="leaveRules" ref="leaveForm" label-width="80px">
                <el-form-item label="请假原因" prop="reason">
                    <el-input v-model="leaveForm.reason" type="textarea" :rows="4" placeholder="请输入请假原因" />
                </el-form-item>
            </el-form>
            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogLeaveVisible = false">取消</el-button>
                <el-button type="primary" @click="submitLeave">提交</el-button>
            </span>
        </el-dialog>
    </div>
</template>

<script>
    // 新增二维码生成库
    import QRCode from 'qrcode'
    import { faceCheck } from "@/api/meeting/face";
    import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect } from "@/api/system/user";
    import { checkMeeting, listMeeting, getUserAllMeeting, getMeeting, delMeeting, addMeeting, updateMeeting, getUserList } from "@/api/meeting/meeting";
    import { generateCheckToken, signStatus } from "@/api/meeting/qrcode";
    import Treeselect from "@riophae/vue-treeselect";
    import "@riophae/vue-treeselect/dist/vue-treeselect.css";
    import { addLeave } from "@/api/meeting/leave"
    import { getAttendance } from "@/api/meeting/attendance"
    export default {
        name: "MyMeeting",
        components: { Treeselect },
        data() {
            return {
                currentMeetingStatus: '0',//当前会议状态
                showQrCode: false,
                showManualCheck: false,
                showFaceRecognition: false,
                faceRecognitionTips: '',
                mediaStreamTrack: null,
                signStatus: '', // 'loading' | 'success' | 'error'
                pollInterval: 6000, // 轮询间隔2秒
                pollTimer: null, // 轮询定时器
                // 新增弹窗控制状态
                dialogCheckVisible: false,
                dialogLeaveVisible: false,
                // 添加验证规则
                leaveRules: {
                    reason: [
                        { required: true, message: '请输入请假原因', trigger: 'blur' }
                    ]
                },
                leaveForm: {
                    reason: ''
                },
                checkActionType: 'checkIn', // checkIn/checkOut
                currentMeetingType: 0, //会议类型：线上/线下
                currentMeetingId: null,
                currentMeetingName: null,
                leaveForm: {
                    meetingId: null,
                    meetingName: null,
                    userId: null,
                    userName: null,
                    reason: ''
                },
                checkDialogTitle: '',
                // 过滤掉已禁用部门树选项
                enabledDeptOptions: undefined,
                selectMode: 'dept', // 选择模式 dept/user
                userOptions: [],    // 人员列表数据
                //会议室
                roomOptions: [],
                //隐藏
                // 遮罩层
                loading: true,
                // 选中数组
                ids: [],
                // 非单个禁用
                single: true,
                // 非多个禁用
                multiple: true,
                // 显示查询条件
                showSearch: true,
                // 总条数
                total: 0,
                // 会议记录表格数据
                meetingList: [],
                // 弹出层标题
                title: "",
                // 是否显示弹出层
                open: false,
                // 查询参数
                queryParams: {
                    pageNum: 1,
                    pageSize: 10,
                    meetingName: null,
                    describe: null,
                    userId: null,
                    userName: null,
                    memberNumber: null,
                    type: null,
                    roomId: null,
                    startTime: null,
                    endTime: null,
                    timeFrame: null,
                    status: null,
                    isDel: null,
                },
                timeRange: [], // 绑定日期控件
                // 表单参数
                form: {},
                // 表单校验
                rules: {
                    meetingName: [
                        { required: true, message: "会议名称不能为空", trigger: "blur" }
                    ],
                    describe: [
                        { required: true, message: "会议描述不能为空", trigger: "blur" }
                    ],
                    userId: [
                        { required: true, message: "发起人编号不能为空", trigger: "blur" }
                    ],
                    userName: [
                        { required: true, message: "发起人姓名不能为空", trigger: "blur" }
                    ],
                    type: [
                        { required: true, message: "会议类型不能为空", trigger: "change" }
                    ],
                    startTime: [
                        { required: true, message: "开始时间不能为空", trigger: "blur" }
                    ],
                    endTime: [
                        { required: true, message: "结束时间不能为空", trigger: "blur" }
                    ],
                    timeFrame: [
                        { required: true, message: "$comment不能为空", trigger: "blur" }
                    ],
                    status: [
                        { required: true, message: "会议状态 0审核中 1已通过 2未通过 3已开始 4已结束不能为空", trigger: "change" }
                    ],
                    isDel: [
                        { required: true, message: "逻辑删除不能为空", trigger: "blur" }
                    ],
                    createTime: [
                        { required: true, message: "创建时间不能为空", trigger: "blur" }
                    ],
                    updateTime: [
                        { required: true, message: "更新时间不能为空", trigger: "blur" }
                    ]
                },
                dialogVisible: false,
                dialogVisibleUserList: false,
                queryResult: [],
                queryUserResult: [],
                typeOptions: [
                    {
                        label: '线上',
                        value: 0
                    },
                    {
                        label: '线下',
                        value: 1
                    }

                ],
                optionStatus: [
                    {
                        label: '审核中',
                        value: '0'
                    },
                    {
                        label: '已通过',
                        value: '1'
                    },
                    {
                        label: '未通过',
                        value: '2'
                    },
                    {
                        label: '已开始',
                        value: '3'
                    },
                    {
                        label: '已结束',
                        value: '4'
                    },


                ],
                // data中定义
                pickerOptions: {
                    shortcuts: [{
                        text: '最近一周',
                        onClick(picker) {
                            const end = new Date()
                            const start = new Date()
                            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
                            picker.$emit('pick', [start, end])
                        }
                    }, {
                        text: '本月',
                        onClick(picker) {
                            const end = new Date()
                            const start = new Date()
                            start.setDate(1)
                            picker.$emit('pick', [start, end])
                        }
                    }]
                },
                qrRefreshTimer: null, // 二维码刷新定时器
            };
        },
        created() {
            this.getUserMeetingList();
        },
        // 在提交请求前处理空值
        beforeSearch() {
            if (this.timeRange === null || this.timeRange.length !== 2) {
                this.queryParams.startTime = null
                this.queryParams.endTime = null
            }
        },
        watch: {
            dialogCheckVisible(newVal) {
                if (!newVal) {
                    clearInterval(this.pollTimer)
                    this.signStatus = ''
                    // 清除二维码刷新定时器
                    if (this.qrRefreshTimer) {
                        clearInterval(this.qrRefreshTimer);
                        this.qrRefreshTimer = null;
                    }
                }
            },
            showQrCode(newVal) {
                if (newVal) {
                    this.generateQrCodeForCheck()
                } else {
                    // 清除轮询
                    clearInterval(this.pollTimer)
                }
            }
        },

        beforeDestroy() {
            if (this.pollTimer) {
                clearInterval(this.pollTimer);
                this.pollTimer = null;
            }
            if (this.qrRefreshTimer) {
                clearInterval(this.qrRefreshTimer);
                this.qrRefreshTimer = null;
            }
        },


        methods: {

            // 获取当前步骤
            getActiveStep(status) {
                switch (status) {
                    case '0': return 0; // 审核中
                    case '1': return 1; // 已通过
                    case '2': return 2; // 未通过
                    case '3': return 3; // 已开始
                    case '4': return 4; // 已结束
                    default: return 0;
                }
            },
            handleTimeRangeChange(range) {
                if (range && range.length === 2) {
                    const start = range[0] ? `${range[0].substr(0, 10)} 00:00:00` : null
                    const end = range[1] ? `${range[1].substr(0, 10)} 23:59:59` : null
                    this.queryParams.startTime = start
                    this.queryParams.endTime = end
                } else {
                    this.queryParams.startTime = null
                    this.queryParams.endTime = null
                }
            },

            handleQueryRooms(row) {
                this.dialogVisible = true; // 显示弹窗
                this.queryResult = []; // 关键修复：立即清空旧数据
                getMeeting(row.meetingId).then(result => {
                    console.log('会议室数据:', result.data);
                    if (result.data.roomId != null) {
                        this.queryResult = result.data.meetingRooms || [];
                    }
                    // this.queryResult = []
                }).catch(error => {
                    this.queryResult = []; // 异常时重置为空数组 
                    this.$message.warning('查询暂无相关会议室信息');
                });
            },

            handleQueryPersonal(row) {
                this.dialogVisibleUserList = true;
                this.currentMeetingStatus = row.status;
                getUserList(row.meetingId).then(response => {
                    this.queryUserResult = response.data;
                }).catch(error => {
                    this.queryUserResult = [];
                });
            },

            // 在methods中添加
            handleDeptChange(deptId) {
                if (!deptId) {
                    this.form.memberNumber = null;
                    return;
                }

                // 调用接口获取部门用户
                listUser({ deptId: deptId }).then(response => {

                    //反显按部门选择的人员列表
                    // console.log("用户列表：" + response.rows);

                    this.queryUserResult = response.rows;
                    console.log("当前用户列表数据：" + this.queryUserResult);


                    const userIds = response.rows.map(user => user.userId);
                    console.log("用户ids" + userIds);

                    // 格式化成数据库需要的字符串格式（如："[1,2,3]"）
                    this.form.memberNumber = `[${userIds.join(',')}]`;

                }).catch(error => {
                    this.$message.error('获取部门成员失败');
                    console.error('部门用户获取失败：', error);
                });
            },

            /** 查询会议记录列表 */
            getUserMeetingList() {
                this.loading = true;
                getUserAllMeeting(this.queryParams).then(response => {
                    this.meetingList = response.rows;
                    console.log("data：" + this.meetingList);

                    this.total = response.total;
                    console.log("总条数：" + this.total);

                    console.log("总条数：" + this.total);

                    this.loading = false;
                });
            },
            // 取消按钮
            cancel() {
                this.open = false;
                this.reset();
            },
            // 表单重置
            reset() {
                this.form = {
                    meetingId: null,
                    meetingName: null,
                    describe: null,
                    userId: null,
                    userName: null,
                    memberNumber: null,
                    type: null,
                    roomId: null,
                    startTime: null,
                    endTime: null,
                    timeFrame: null,
                    status: null,
                    isDel: null,
                    createTime: null,
                    updateTime: null,
                    expireTimer: null,
                    actionType: null,
                };
                this.selectMode = 'dept'; // 或根据情况保留当前值
                this.resetForm("form");
            },
            /** 查询按钮操作 */
            handleQuery() {
                // 在查询前添加校验
                if (this.timeRange && this.timeRange.length === 2) {
                    const [start, end] = this.timeRange
                    if (new Date(start) > new Date(end)) {
                        this.$message.error("结束时间不能早于开始时间")
                        return
                    }
                }

                this.queryParams.pageNum = 1;
                this.getUserMeetingList();
            },
            /** 重置按钮操作 */
            resetQuery() {
                this.resetForm("queryForm");
                // 清空时间范围控件
                this.timeRange = [];
                // 清空查询参数中的时间
                this.queryParams.startTime = null;
                this.queryParams.endTime = null;
                this.handleQuery();
            },
            // 多选框选中数据
            handleSelectionChange(selection) {
                this.ids = selection.map(item => item.meetingId)
                this.single = selection.length !== 1
                this.multiple = !selection.length
            },
            /** 新增按钮操作 */
            handleAdd() {
                this.reset();
                getMeeting().then(response => {
                    this.open = true;
                    this.roomOptions = response.meetingRooms;
                    this.title = "预约会议";
                })
            },
            /** 修改按钮操作 */
            handleUpdate(row) {
                this.reset();
                const meetingId = row.meetingId || this.ids
                getMeeting(meetingId).then(response => {
                    this.form = response.data;
                    this.roomOptions = response.meetingRooms;
                    if (this.form.memberNumber) {
                        try {
                            // 1. 解析member_number字符串为数组
                            const memberIds = JSON.parse(this.form.memberNumber.replace(/'/g, '"'));

                            console.log("字符串：" + memberIds);

                            // 2. 匹配用户选项中的对应项
                            this.form.userIds = this.userOptions
                                .filter(user => memberIds.includes(user.userId))
                                .map(user => user.userId);
                            console.log("选中的" + this.form.userIds);
                            this.selectMode = 'user';
                        } catch (e) {
                            console.error('成员数据解析失败：', e);
                            this.selectMode = 'dept';
                        }
                    } else {
                        this.selectMode = 'dept';
                    }

                    this.open = true;
                    this.title = "修改会议";
                });
            },
            /** 提交按钮 */
            submitForm() {
                this.$refs["form"].validate(valid => {
                    if (valid) {
                        if (this.form.meetingId != null) {
                            updateMeeting(this.form).then(response => {
                                this.$modal.msgSuccess("修改成功");
                                this.open = false;
                                this.getUserMeetingList();
                            });
                        } else {
                            addMeeting(this.form).then(response => {
                                this.$modal.msgSuccess("新增成功");
                                this.open = false;
                                this.getUserMeetingList();
                            });
                        }
                    }
                });
            },
            /** 删除按钮操作 */
            handleDelete(row) {
                const meetingIds = row.meetingId || this.ids;
                this.$modal.confirm('是否确认删除会议记录编号为"' + meetingIds + '"的数据项？').then(function () {
                    return delMeeting(meetingIds);
                }).then(() => {
                    this.getUserMeetingList();
                    this.$modal.msgSuccess("删除成功");
                }).catch(() => { });
            },
            /** 导出按钮操作 */
            handleExport() {
                this.download('/meeting/info/exportUserPrivate', {
                    ...this.queryParams
                }, `meeting_${new Date().getTime()}.xlsx`)
            },


            // 修改操作列方法
            handleCheckIn(row) {
                if (row.status === '4') {
                    this.$message.warning('会议已结束');
                    return;
                }
                this.handleCheckAction(row, 'checkIn')
            },
            handleCheckOut(row) {
                if (row.status === '4') {
                    this.$message.warning('会议已结束');
                    return;
                }
                this.handleCheckAction(row, 'checkOut')
            },
            handleLeave(row) {
                if (row.status === '4') {
                    this.$message.warning('会议已结束');
                    return;
                }
                console.log("当前数据：", row);

                this.leaveForm = {
                    meetingId: row.meetingId,
                    meetingName: row.meetingName,
                    userId: null,
                    userName: null,
                    reason: '',
                }
                this.dialogLeaveVisible = true
            },



            async handleCheckAction(row, actionType) {
                try {
                    this.dialogCheckVisible = true
                    this.currentMeetingId = row.meetingId
                    this.currentMeetingName = row.meetingName
                    this.currentMeetingType = row.type
                    this.checkActionType = actionType
                    this.checkDialogTitle = `${actionType === 'checkIn' ? '会议签到' : '会议签退'} - ${row.meetingName}`

                    // 重置显示状态
                    this.showQrCode = false
                    this.showManualCheck = false
                    this.showFaceRecognition = false

                } catch (err) {
                    console.error('操作失败:', err)
                    this.$message.error('获取签到信息失败')
                    this.dialogCheckVisible = false
                }
            },

            // 修改二维码生成方法，在选择二维码签到时调用
            async generateQrCodeForCheck() {
                // 确保所有必要参数都存在
                const params = {
                    meetingId: this.currentMeetingId,
                    actionType: this.checkActionType,
                    meetingName: this.currentMeetingName,
                    checkMethod: 'qrcode', // 添加签到/签退方式标记
                    _t: Date.now() // 添加时间戳
                }


                console.log('生成二维码参数:', params); // 添加日志

                const response = await generateCheckToken(params);

                if (response.code === 200) {
                    await this.generateQRCode(response.msg);
                    // 触发轮询
                    this.startStatusPolling(params);
                    // 设置定时器，每隔60秒刷新二维码
                    if (this.qrRefreshTimer) {
                        clearInterval(this.qrRefreshTimer);
                    }
                    this.qrRefreshTimer = setInterval(() => {
                        this.generateQrCodeForCheck();
                    }, 60000); // 60秒刷新一
                } else {
                    this.$message.error(response.msg || '生成二维码失败');
                }

            },

            // 简化二维码生成方法
            async generateQRCode(url) {
                try {
                    await this.$nextTick()
                    const canvas = this.$refs.qrCanvas
                    const ctx = canvas.getContext('2d')
                    ctx.clearRect(0, 0, canvas.width, canvas.height)

                    // 直接使用后端返回的URL生成二维码
                    await QRCode.toCanvas(canvas, url, {
                        width: 200,
                        margin: 2,
                        errorCorrectionLevel: 'H'
                    })
                } catch (err) {
                    console.error('生成二维码失败:', err)
                    this.$message.error('二维码生成失败，请重试')
                    this.resetCheckMethod();
                }
            },


            // 线下确认签到/签退
            async handleConfirmCheck() {
                console.log("签到的会议" + this.currentMeetingId);

                try {
                    const res = await checkMeeting({
                        meetingId: this.currentMeetingId,
                        meetingName: this.currentMeetingName,
                        actionType: this.checkActionType,
                        type: this.currentMeetingType,
                        checkMethod: 'manual' // 添加签到/签退方式标记
                    })

                    // 根据消息类型显示对应提示
                    if (res.data.code === 200) {
                        this.$message.success(res.data.msg);
                        // 成功操作后刷新列表
                        this.dialogCheckVisible = false;
                    } else {
                        // 业务提示类消息（如重复签到）用warning类型更合适
                        this.$message.info(res.data.msg);
                        this.dialogCheckVisible = false;
                    }
                } catch (error) {
                    // 异常处理（网络错误/系统错误）
                    const errorMsg = error.response?.data?.msg || '操作失败，请检查网络';
                    this.$message.error(errorMsg);
                    console.error("接口调用失败:", error);

                    // 失败时保持弹窗可见以便重试
                    this.dialogCheckVisible = true;
                }
            },
            // 处理二维码签到/签退选择
            handleQrCodeCheck() {
                // 先重置状态
                this.resetCheckMethod();
                this.showQrCode = true;
                this.showManualCheck = false;
                this.showFaceRecognition = false;

                // 添加延时确保DOM已更新
                this.$nextTick(() => {
                    this.generateQrCodeForCheck();
                });
            },

            // 处理手动签到/签退选择
            handleManualCheck() {
                this.showManualCheck = true;
                this.showQrCode = false;
                this.showFaceRecognition = false;
            },

            // 重置签到方式选择
            resetCheckMethod() {
                this.showQrCode = false;
                this.showManualCheck = false;
                this.showFaceRecognition = false;
                this.signStatus = ''; // 重置签到状态

                // 清除轮询
                if (this.pollTimer) {
                    clearInterval(this.pollTimer);
                    this.pollTimer = null;
                }


                // 清除二维码刷新定时器
                if (this.qrRefreshTimer) {
                    clearInterval(this.qrRefreshTimer);
                    this.qrRefreshTimer = null;
                }

                // 如果有摄像头开启，需要关闭
                if (this.mediaStreamTrack) {
                    this.closeMedia();
                }
            },
            // 新增轮询方法
            startStatusPolling(params) {
                if (this.pollTimer) {
                    clearInterval(this.pollTimer)
                    this.pollTimer = null
                }
                this.pollTimer = setInterval(async () => {
                    try {
                        const res = await signStatus({
                            meetingId: params.meetingId,
                            actionType: params.actionType,
                        })

                        // 处理不同状态
                        switch (res.status) {
                            case 'pending':
                                // 保持二维码显示，不改变状态
                                break;
                            case 'processing':
                                this.signStatus = 'loading';
                                break;
                            case 'already_done':
                                this.handleSignalreadyDone()
                                break;
                            case 'leave':
                                this.handleSignLeave()
                                break;
                            case 'success':
                                this.handleSignSuccess()
                                break;
                            case 'error':
                                this.handleSignError()
                                break;
                            case 'need_check_in':
                                this.handleSignNeedCheckIn()
                                break;
                            default:
                                this.signStatus = ''
                        }
                    } catch (error) {
                        this.handleSignError()
                    }
                }, this.pollInterval)
            },

            // 处理成功状态
            handleSignSuccess() {
                clearInterval(this.pollTimer)
                this.signStatus = 'success'
                setTimeout(() => {
                    this.dialogCheckVisible = false
                    this.getUserMeetingList()
                    // this.signStatus = ''
                }, 2000)
            },

            handleSignalreadyDone() {
                clearInterval(this.pollTimer)
                this.pollTimer = null; // 确保定时器被正确清除
                this.signStatus = 'already_done'
                setTimeout(() => {
                    this.dialogCheckVisible = false
                    this.getUserMeetingList()
                    // this.signStatus = ''
                }, 2000)
            },
            handleSignLeave() {
                clearInterval(this.pollTimer)
                this.pollTimer = null; // 确保定时器被正确清除
                this.signStatus = 'leave'
                setTimeout(() => {
                    this.dialogCheckVisible = false
                    this.getUserMeetingList()
                    // this.signStatus = ''
                }, 2000)
            },

            handleSignNeedCheckIn() {
                clearInterval(this.pollTimer)
                this.pollTimer = null; // 确保定时器被正确清除
                this.signStatus = 'need_check_in'
                setTimeout(() => {
                    this.dialogCheckVisible = false
                    this.getUserMeetingList()
                    // this.signStatus = ''
                }, 2000)
            },
            // 处理失败状态
            handleSignError() {
                clearInterval(this.pollTimer)
                this.pollTimer = null; // 确保定时器被正确清除
                this.signStatus = 'error'
                setTimeout(() => {
                    this.dialogCheckVisible = false
                    this.getUserMeetingList()
                    this.signStatus = ''
                }, 2000)
            },



            // 提交请假
            async submitLeave() {
                const res = await addLeave(this.leaveForm)
                console.log("请假数据：" + res);
                this.$refs.leaveForm.validate(valid => {
                    if (valid) {
                        if (res.data.code === 200) {
                            this.$message.success(res.data.msg)
                            this.dialogLeaveVisible = false
                            this.getUserMeetingList()
                        }
                        else {
                            this.$message.info(res.data.msg)
                        }
                        this.dialogLeaveVisible = false
                    } else {
                        // 验证不通过时的处理
                        return false
                    }
                });

            },
            // 处理人脸识别签到
            async handleFaceRecognition() {
                this.showFaceRecognition = true;
                this.showQrCode = false;
                this.showManualCheck = false;
                // 确保DOM更新后再打开摄像头
                await this.$nextTick();
                this.openMedia();
            },

            // 打开摄像头
            async openMedia() {
                try {
                    const that = this;
                    that.faceRecognitionTips = '正在打开摄像头';
                    const constraints = { video: { width: 400, height: 300 }, audio: false };
                    const video = document.getElementById('video');

                    const mediaStream = await navigator.mediaDevices.getUserMedia(constraints);
                    this.mediaStreamTrack = typeof mediaStream.stop === 'function' ? mediaStream : mediaStream.getTracks()[0];
                    video.srcObject = mediaStream;
                    await video.play();

                    that.faceRecognitionTips = '请正视摄像头';
                    setTimeout(() => that.takeFacePhoto(), 3000);
                } catch (error) {
                    console.error('摄像头启动失败:', error);
                    that.faceRecognitionTips = '摄像头启动失败';
                };
            },

            // 关闭摄像头
            closeMedia() {
                if (this.mediaStreamTrack) {
                    this.mediaStreamTrack.stop();
                    this.mediaStreamTrack = null
                }
                this.showFaceRecognition = false;
                this.faceRecognitionTips = '';
            },

            // 拍照并进行人脸识别
            async takeFacePhoto() {
                try {
                    const video = document.getElementById('video');
                    const canvas = document.getElementById('canvas');
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(video, 0, 0, 400, 300);

                    const img = canvas.toDataURL();
                    this.faceRecognitionTips = '正在识别';

                    // 调用人脸识别接口
                    const res = await faceCheck({
                        meetingId: this.currentMeetingId,
                        meetingName: this.currentMeetingName,
                        actionType: this.checkActionType,
                        type: this.currentMeetingType,
                        faceImage: img,
                        checkMethod: 'face' // 添加签到/签退方式标记
                    });
                    // 立即关闭摄像头
                    this.closeMedia();

                    if (res.data.code === 200) {
                        this.signStatus = 'success';
                        this.$message.success(res.data.msg);
                    } else {
                        this.signStatus = 'error';
                        this.$message.info(res.data.msg);
                    }
                    // 延迟关闭对话框
                    setTimeout(() => {
                        this.dialogCheckVisible = false;
                    }, 2000);
                } catch (err) {
                    // 发生错误时也要关闭摄像头
                    this.closeMedia();
                    this.signStatus = 'error';
                    setTimeout(() => {
                        this.dialogCheckVisible = false;
                    }, 2000);
                }
            },

            // 返回状态标签文本
            getStatusLabel(status) {
                switch (status) {
                    case '0': return '审核中';
                    case '1': return '已通过';
                    case '2': return '未通过';
                    case '3': return '已开始';
                    case '4': return '已结束';

                }
            },
            // 返回对应的颜色类型
            getStatusType(status) {
                switch (status) {
                    case '0': return 'warning';  // 黄色
                    case '1': return 'success';  // 绿色
                    case '2': return 'danger';  // 绿色
                    // case '3': return 'success';  // 绿色
                    case '4': return 'info';  // 绿色

                }
            },
            getMeetingType(type) {
                switch (type) {
                    case 1: return 'info';  // 绿色

                }
            },
            getMeetingLabel(type) {
                switch (type) {
                    case 0: return '线上';
                    case 1: return '线下';


                }
            },
            getSignInStatusLabel(status) {
                switch (status) {
                    case '0': return '未签到';
                    case '1': return '已签到';
                    case '2': return '迟到';
                    case '3': return '代签到';
                }
            },
            // 返回对应的颜色类型
            getSignInStatusType(status) {
                switch (status) {
                    case '0': return 'warning';  // 黄色
                    case '1': return 'success';  // 绿色
                    case '2': return 'danger';  // 绿色

                }
            },
            getSignOutStatusLabel(status) {
                switch (status) {
                    case '0': return '未签退';
                    case '1': return '已签退';
                    case '2': return '早退';
                    case '3': return '代签退';
                }
            },
            // 返回对应的颜色类型
            getSignOutStatusType(status) {
                switch (status) {
                    case '0': return 'warning';  // 黄色
                    case '1': return 'success';  // 绿色
                    case '2': return 'danger';  // 绿色

                }
            },
            // 返回状态标签文本
            getIsLeaveStatusLabel(status) {
                switch (status) {
                    case '0': return '否';
                    case '1': return '是';

                }
            },
            // 返回对应的颜色类型
            getIsLeaveStatusType(status) {
                switch (status) {
                    case '0': return 'info';  // 黄色
                }
            }
        }
    };
</script>

<style scoped>
    .qr-code-container {
        margin-top: 20px;
        text-align: center;
    }

    .qr-code-wrapper {
        padding: 20px;
        background: #fff;
        display: inline-block;
        border: 1px solid #ebeef5;
        border-radius: 4px;
    }

    .tip-text {
        color: #909399;
        font-size: 14px;
        margin-top: 15px;
    }

    .offline-check {
        text-align: center;
        padding: 20px 0;
    }

    .tip-text {
        color: #909399;
        font-size: 14px;
        margin-top: 15px;
    }

    .status-container {
        text-align: center;
        min-height: 200px;
        display: flex;
        align-items: center;
        justify-content: center;
    }

    .success-status {
        color: #67C23A;

        i {
            font-size: 60px;
            margin-bottom: 15px;
            animation: bounceIn 0.75s;
        }

        p {
            font-size: 18px;
        }
    }

    .error-status {
        color: #F56C6C;

        i {
            font-size: 60px;
            margin-bottom: 15px;
        }

        p {
            font-size: 18px;
        }
    }

    .loading-status {
        color: #409EFF;

        i {
            font-size: 40px;
            margin-bottom: 15px;
            animation: rotating 2s linear infinite;
        }

        p {
            font-size: 16px;
        }
    }

    .pending-tip {
        color: #909399;
        margin-top: 10px;
        font-size: 14px;
    }

    /* 已处理状态 */
    .done-status {
        color: #E6A23C;

        i {
            font-size: 60px;
            margin-bottom: 15px;
        }

        p {
            font-size: 18px;
        }
    }

    /* 处理中状态 */
    .processing-status {
        color: #409EFF;

        i {
            font-size: 40px;
            margin-bottom: 15px;
            animation: rotating 2s linear infinite;
        }

        p {
            font-size: 16px;
        }
    }

    /* 请假状态 */
    .leave-status {
        color: #909399;

        i {
            font-size: 60px;
            margin-bottom: 15px;
            color: #F56C6C;
        }

        p {
            font-size: 18px;
            color: #F56C6C;
        }
    }

    .manual-check {
        margin-top: 20px;
        text-align: center;
    }

    /* 确保按钮组样式美观 */
    .el-row {
        margin-bottom: 20px;
    }

    .check-container {
        position: relative;
        padding-top: 20px;
    }

    .back-button {
        position: absolute;
        top: 0;
        left: 0;
        margin-bottom: 15px;
    }

    .check-options {
        margin: 20px 0;
    }

    .check-options .el-row {
        display: flex;
        justify-content: space-around;
    }

    .check-options .el-col {
        text-align: center;
    }

    .check-options .el-button {
        width: 120px;
    }


    /* 人脸识别 */

    .face-recognition-container {
        margin-top: 20px;
        text-align: center;
    }

    .face-recognition-container video {
        border-radius: 8px;
        border: 2px solid #ebeef5;
        margin-bottom: 10px;
    }

    .face-recognition-container .tip-text {
        color: #909399;
        font-size: 14px;
        margin-top: 10px;
    }

    .need-checkin-status {
        color: #E6A23C;

        i {
            font-size: 60px;
            margin-bottom: 15px;
        }

        p {
            font-size: 18px;
        }
    }

    /* 旋转动画 */
    @keyframes rotating {
        from {
            transform: rotate(0deg);
        }

        to {
            transform: rotate(360deg);
        }
    }

    @keyframes bounceIn {
        0% {
            transform: scale(0.9);
            opacity: 0;
        }

        50% {
            transform: scale(1.05);
        }

        100% {
            transform: scale(1);
            opacity: 1;
        }
    }

    @keyframes rotating {
        from {
            transform: rotate(0deg);
        }

        to {
            transform: rotate(360deg);
        }
    }
</style>