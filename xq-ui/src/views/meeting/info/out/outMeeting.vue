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
                <el-button type="primary" round plain icon="el-icon-plus" size="mini" @click="handleAdd"
                    v-hasPermi="['meeting:info:add']">发起会议</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="primary" round plain icon="el-icon-edit" size="mini" :disabled="single"
                    @click="handleUpdate" v-hasPermi="['meeting:info:edit']">修改</el-button>
            </el-col>
            <el-col :span="1.5">
                <el-button type="primary" round plain icon="el-icon-delete" size="mini" :disabled="multiple"
                    @click="handleDelete" v-hasPermi="['meeting:info:remove']">删除</el-button>
            </el-col>
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
            <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
                <template slot-scope="scope">
                    <el-button size="mini" type="text" @click="handleUpdate(scope.row)"
                        v-hasPermi="['meeting:info:edit']">修改</el-button>
                    <el-button size="mini" type="text" @click="handleDelete(scope.row)"
                        v-hasPermi="['meeting:info:remove']">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
            @pagination="getList" />


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
        <el-dialog title="参会人员签到情况" :visible.sync="dialogVisibleUserList" width="65%" style="margin-top: 50px;"
            :modal-append-to-body="false" :body-style="{ maxHeight: '60vh', overflowY: 'auto' }">
            <!-- 会议未通过时显示红色提示 -->
            <div v-if="currentMeetingStatus === '2'"
                style="text-align: center; margin-bottom: 20px; padding: 10px; background-color: #FEF0F0; border-radius: 4px;">
                <i class="el-icon-error"
                    style="color: #F56C6C; font-size: 24px; vertical-align: middle; margin-right: 8px;"></i>
                <span style="color: #F56C6C; font-size: 16px; font-weight: bold;">审批未通过</span>
            </div>
            <!-- 其他状态显示正常步骤条 -->
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

                <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
                    <template slot-scope="scope">
                        <el-button size="mini" type="text" @click="handleCheckIn(scope.row)"
                            v-hasPermi="['meeting:info:check:in']">代签到</el-button>
                        <el-button size="mini" type="text" @click="handleCheckOut(scope.row)"
                            v-hasPermi="['meeting:info:check:out']">代签退</el-button>
                    </template>
                </el-table-column>
            </el-table>

            <span slot="footer" class="dialog-footer">
                <el-button @click="dialogVisibleUserList = false">关闭</el-button>
            </span>
        </el-dialog>



        <!-- 添加或修改会议记录对话框 -->
        <el-dialog :title="title" :visible.sync="open" width="680px" append-to-body>
            <el-form ref="form" :model="form" :rules="rules" label-width="100px">
                <el-form-item label="会议名称" prop="meetingName">
                    <el-input v-model="form.meetingName" placeholder="请输入会议名称" />
                </el-form-item>
                <el-form-item label="会议描述" prop="meetingDesc">
                    <el-input v-model="form.meetingDesc" placeholder="请输入会议描述" />
                </el-form-item>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="会议类型" align="center" prop="type">
                            <el-select v-model="form.type" clearable placeholder="请选择会议类型">
                                <el-option v-for="item in typeOptions" :key="item.value" :label="item.label"
                                    :value="item.value" :disabled="item.status == 1"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">
                        <el-form-item label="会议室" prop="meetingRooms">
                            <el-select v-model="form.roomId" :disabled="form.type === 0" clearable
                                :placeholder="form.type === 0 ? '线上会议无需选择' : '请选择会议室'">
                                <el-option v-for="item in roomOptions" :key="item.roomId" :label="item.roomName"
                                    :value="item.roomId" :disabled="item.status == 1"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-form-item label="参会成员">
                    <el-row>
                        <el-col :span="12">
                            <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true"
                                placeholder="请选择归属部门" :disabled="selectMode === 'user'" @input="handleDeptChange"
                                style="width: 220px;" />
                        </el-col>

                        <!-- 人员多选 -->
                        <el-col :span="12">
                            <el-select v-model="form.userIds" multiple filterable placeholder="请选择参会人员"
                                style="width: 100%" :disabled="selectMode === 'dept'" @change="handleUserChange">
                                <el-option v-for="user in userOptions" :key="user.userId" :label="user.userName"
                                    :value="user.userId">
                                    <span style="float: left">{{ user.userName }} / {{ user.nickName }}</span>
                                    <span style="float: right; color: #8492a6; font-size: 13px">
                                        {{ user.deptName }} / {{ user.phonenumber }}
                                    </span>
                                </el-option>
                            </el-select>
                        </el-col>
                    </el-row>
                </el-form-item>
                <el-form-item v-if="form.meetingId !=null " label="会议状态" prop="status">
                    <el-select v-model="form.status" clearable placeholder="默认状态">
                        <el-option v-for="item in optionStatus" :key="item.value" :label="item.label"
                            :value="item.value" :disabled="item.status == 1"></el-option>
                    </el-select>
                </el-form-item>
                <el-row>
                    <el-col :span="12">
                        <el-form-item label="开始时间" prop="startTime">
                            <el-date-picker clearable v-model="form.startTime" :picker-options="pickerOption"
                                type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择开始时间">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="12">

                        <el-form-item label="结束时间" prop="endTime">
                            <el-date-picker clearable v-model="form.endTime" :picker-options="pickerOption"
                                type="datetime" value-format="yyyy-MM-dd HH:mm:ss" placeholder="请选择结束时间">
                            </el-date-picker>
                        </el-form-item>
                    </el-col>
                </el-row>


            </el-form>
            <div slot="footer" class="dialog-footer">
                <el-button type="primary" @click="submitForm">确 定</el-button>
                <el-button @click="cancel">取 消</el-button>
            </div>
        </el-dialog>
    </div>
</template>

<script>
    import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect } from "@/api/system/user";
    import { listMeeting, getMeeting, delMeeting, addMeeting, updateMeeting, getUserList, signatureMeeting } from "@/api/meeting/meeting";
    import Treeselect from "@riophae/vue-treeselect";
    import "@riophae/vue-treeselect/dist/vue-treeselect.css";
    export default {
        name: "OutMeeting",
        components: { Treeselect },
        data() {
            return {
                currentMeetingStatus: '0',//当前会议状态
                // 过滤掉已禁用部门树选项
                enabledDeptOptions: undefined,
                selectMode: 'none', // 选择模式 dept/user
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
                    startTime: '',
                    endTime: '',
                    timeFrame: null,
                    status: null,
                    isDel: null,
                },
                timeRange: [],
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
                    ],

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
                pickerOption: {
                    shortcuts: [
                        {
                            text: '半小时后',
                            onClick(picker) {
                                const date = new Date();
                                date.setTime(date.getTime() + 30 * 60 * 1000); // 半小时
                                picker.$emit('pick', date);
                            }
                        },
                        {
                            text: '一小时后',
                            onClick(picker) {
                                const date = new Date();
                                date.setTime(date.getTime() + 60 * 60 * 1000); // 一小时
                                picker.$emit('pick', date);
                            }
                        },
                        {
                            text: '明天此刻',
                            onClick(picker) {
                                const date = new Date();
                                date.setTime(date.getTime() + 24 * 60 * 60 * 1000); // 明天
                                picker.$emit('pick', date);
                            }
                        }
                    ],
                },
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
                }
            };
        },
        created() {
            this.getList();
            this.getDeptTree();
            this.getUser();
        },
        watch: {
            'form.type'(newVal) {
                if (newVal === 0) { // 当切换为线上会议时
                    this.form.roomId = null // 清空已选会议室
                }
            }
        },
        beforeSearch() {
            if (this.timeRange === null || this.timeRange.length !== 2) {
                this.queryParams.startTime = null
                this.queryParams.endTime = null
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
            getUser() {
                listUser({
                    pageNum: 1,
                    pageSize: 1000 // 设置足够大的分页大小获取所有用户
                }).then(response => {
                    console.log("用户列表：" + response);
                    this.userOptions = response.rows
                    // 新增：创建快速查找Map
                    this.userMap = new Map();
                    response.rows.forEach(user => {
                        this.userMap.set(user.userId, user);
                    });
                })
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

            /** 查询部门下拉树结构 */
            getDeptTree() {
                deptTreeSelect().then(response => {
                    this.deptOptions = response.data;
                    this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(response.data)));

                });
            },
            // 过滤禁用的部门
            filterDisabledDept(deptList) {
                return deptList.filter(dept => {
                    if (dept.disabled) {
                        return false;
                    }
                    if (dept.children && dept.children.length) {
                        dept.children = this.filterDisabledDept(dept.children);
                    }
                    return true;
                });
            },

            handleQueryRooms(row) {
                this.dialogVisible = true; // 显示弹窗
                this.queryResult = []; // 关键修复：立即清空旧数据
                getMeeting(row.meetingId).then(result => {
                    if (result.data.roomId != null) {
                        this.queryResult = result.data.meetingRooms || [];
                    }
                }).catch(error => {
                    this.queryResult = []; // 异常时重置为空数组 
                });
            },

            handleQueryPersonal(row) {
                if (this.queryUserResult = []) {
                    this.dialogVisibleUserList = true; // 显示弹窗
                    this.currentMeetingStatus = row.status; // 保存当前会议状态
                    getUserList(row.meetingId).then(response => {
                        this.queryUserResult = response.data
                        console.log("签到人员的数据：" + this.queryUserResult);

                    }).catch(error => {
                        this.queryUserResult = []; // 异常时重置为空数组 
                    });
                }


            },
            handleUserChange(userIds) {
                if (!userIds || userIds.length === 0) {
                    this.selectMode = 'none';
                    this.form.deptId = null;
                    this.form.memberNumber = "[]";
                    return;
                }

                this.selectMode = 'user';
                this.form.deptId = null;
                // 确保 userOptions 中有数据
                if (this.userOptions.length === 0) {
                    this.getUser(); // 重新获取用户列表
                }
                this.form.memberNumber = JSON.stringify(userIds); // 将选中的用户 ID 数组合并到 memberNumber
            },

            // 在methods中添加
            handleDeptChange(deptId) {
                if (!deptId) {
                    this.selectMode = 'none';
                    this.form.userIds = [];
                    this.form.memberNumber = "[]";
                    return;
                }

                this.selectMode = 'dept';
                this.form.userIds = []

                // 调用接口获取部门用户
                listUser({
                    deptId: deptId,
                    pageNum: 1,
                    pageSize: 1000 // 获取足够多的用户数据
                }).then(response => {

                    const userIds = response.rows.map(user => user.userId);
                    this.form.memberNumber = JSON.stringify(userIds);
                    // 更新人员多选组件的选中状态
                    this.form.userIds = [];  // 先清空
                    this.$nextTick(() => {
                        this.form.userIds = userIds; // 在下一个 tick 设置新值
                    });
                }).catch(error => {
                    this.$message.error('获取部门成员失败');
                    this.form.memberNumber = "[]"; // 异常时设置为空数组的 JSON 字符串
                    console.error('部门用户获取失败：', error);
                });
            },

            /** 查询会议记录列表 */
            getList() {
                this.loading = true;
                listMeeting(this.queryParams).then(response => {
                    this.meetingList = response.rows;
                    console.log("符合条件的数据：" + this.meetingList);

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
                    userIds: [], // 确保重置用户选择
                    deptId: null // 确保重置部门选择
                };
                this.selectMode = 'none'
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
                this.getList();
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
                    this.roomOptions = response.meetingRooms || [];
                    // 确保 deptId 被正确赋值
                    if (response.data.deptId) {
                        this.form.deptId = response.data.deptId;
                    }
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
                        } catch (e) {
                            console.error('成员数据解析失败：', e);
                        }
                    }

                    this.open = true;
                    this.title = "修改会议";
                });
            },
            /** 提交按钮 */
            submitForm() {
                this.$refs["form"].validate(valid => {
                    if (valid) {
                        // 确保 memberNumber 不为空
                        if (!this.form.memberNumber || this.form.memberNumber === "[]") {
                            this.$message.error("请选择参会人员");
                            return;
                        }

                        // 验证开始时间不能大于结束时间
                        if (new Date(this.form.startTime) > new Date(this.form.endTime)) {
                            this.$message.error("开始时间不能大于结束时间");
                            return;
                        }
                        if (this.form.meetingId != null) {
                            updateMeeting(this.form).then(response => {
                                this.$modal.msgSuccess("修改成功");
                                this.open = false;
                                this.getList();
                            });
                        } else {
                            addMeeting(this.form).then(response => {
                                this.$modal.msgSuccess("新增成功");
                                this.open = false;
                                this.getList();
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
                    this.getList();
                    this.$modal.msgSuccess("删除成功");
                }).catch(() => { });
            },
            /** 导出按钮操作 */
            handleExport() {
                this.download('/meeting/info/export', {
                    ...this.queryParams
                }, `meeting_${new Date().getTime()}.xlsx`)
            },


            // 修改操作列方法
            handleCheckIn(row) {
                this.handleConfirmCheck(row, 'checkIn')
            },
            handleCheckOut(row) {
                this.handleConfirmCheck(row, 'checkOut')
            },
            // 代签只能手动确认
            async handleConfirmCheck(row, actionType) {
                console.log("代签到的会议" + row.cmsAttendance.meetingId);
                const res = await signatureMeeting({
                    meetingId: row.cmsAttendance.meetingId,
                    meetingName: row.cmsAttendance.meetingName,
                    actionType: actionType,
                    type: 1,
                    userId: row.userId,
                    userName: row.userName
                });

                // 根据消息类型显示对应提示
                if (res.data.code === 200) {
                    this.dialogVisibleUserList = false; // 关闭弹窗
                    this.$message.success(res.data.msg);
                    // 成功操作后刷新列表
                    await this.handleQueryPersonal({ meetingId: row.cmsAttendance.meetingId });
                } else if (res.data.code === 500) {
                    this.dialogVisibleUserList = false; // 关闭弹窗
                    // 业务提示类消息（如重复签到）用warning类型更合适
                    this.$message.info(res.data.msg);
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

            getIsLeaveStatusLabel(isLeave) {
                switch (isLeave) {
                    case '0': return '否';
                    case '1': return '是';

                }
            },
            // 返回对应的颜色类型
            getIsLeaveStatusType(isLeave) {
                switch (isLeave) {
                    case '0': return 'info';  // 黄色

                }
            }

        }
    };
</script>

<style scoped></style>