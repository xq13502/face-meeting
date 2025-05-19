<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="用户姓名" prop="userName">
        <el-input v-model="queryParams.userName" placeholder="请输入用户姓名" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="会议名称" prop="meetingName">
        <el-input v-model="queryParams.meetingName" placeholder="请输入会议名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="是否请假" prop="isLeave">
        <el-select v-model="queryParams.isLeave" placeholder="请选择是否请假" clearable>
          <el-option v-for="dict in opetionIsLeave " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到状态" prop="signInStatus">
        <el-select v-model="queryParams.signInStatus" placeholder="请选择签到状态" clearable>
          <el-option v-for="dict in opetionSignInStatus " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到方式" prop="signInMethod">
        <el-select v-model="queryParams.signInMethod" placeholder="请选择签到方式" clearable>
          <el-option v-for="dict in opetionSignInMethod " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item label="签到时间" prop="signInTime">

        <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange"
          range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" round size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['meeting:attendance:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['meeting:attendance:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="attendanceList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <!-- <el-table-column label="编号" align="center" prop="id" /> -->
      <el-table-column label="序号" align="center" width="120">
        <template slot-scope="scope">
          {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
        </template>
      </el-table-column>
      <!-- <el-table-column label="用户编号" align="center" prop="userId" /> -->
      <el-table-column label="会议名称" show-overflow-tooltip align="center" prop="meetingName" />
      <el-table-column label="用户姓名" align="center" prop="userName" />

      <el-table-column label="签到状态" align="center" prop="signInStatus">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getSignInStatusType(scope.row.signInStatus)" size="small">
            {{ getSignInStatusLabel(scope.row.signInStatus) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到方式" align="center" prop="signInMethod">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag effect="plain" size="small">
            {{ getSignInMethodLabel(scope.row.signInMethod) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签到时间" align="center" prop="signInTime" width="150">
        <template slot-scope="scope">
          <span>{{
            scope.row.signInTime
            ? parseTime(scope.row.signInTime)
            : '暂未签到'
            }}</span>
        </template>
      </el-table-column>
      <el-table-column label="签退状态" align="center" prop="signOutStatus">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag :type="getSignOutStatusType(scope.row.signOutStatus)" size="small">
            {{ getSignOutStatusLabel(scope.row.signOutStatus) }}
          </el-tag>
        </template>
      </el-table-column>

      <el-table-column label=" 签退方式" align="center" prop="signOutMethod">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag effect="plain" size="small">
            {{ getSignOutMethodLabel(scope.row.signOutMethod) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="签退时间" align="center" prop="signOutTime" width="150">
        <template slot-scope="scope">
          <span>
            {{
            scope.row.signOutTime
            ? parseTime(scope.row.signOutTime)
            : '暂未签退'
            }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="是否请假" align="center" prop="isLeave">
        <template slot-scope="scope">
          <!-- 根据状态值渲染不同标签 -->
          <el-tag effect="plain" size="small">
            {{ getIsLeaveLabel(scope.row.isLeave) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button size="mini" type="text" @click="handleUpdate(scope.row)"
            v-hasPermi="['meeting:attendance:edit']">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:attendance:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改签到考勤对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="500px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户姓名" prop="userName">
          <el-input v-model="form.userName" :readonly="true" placeholder="请输入用户姓名" />
        </el-form-item>
        <el-form-item label="会议名称" prop="meetingName">
          <el-input v-model="form.meetingName" :readonly="true" placeholder="请输入会议名称" />
        </el-form-item>
        <el-form-item label="是否请假" prop="isLeave">
          <el-radio-group v-model="form.isLeave">
            <el-radio :label="'1'">是</el-radio>
            <el-radio :label="'0'">否</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="签到方式 " prop="signInMethod">
          <el-radio-group v-model="form.signInMethod">
            <el-radio :label="0">人脸签到</el-radio>
            <el-radio :label="1">扫码签到</el-radio>
            <el-radio :label="2">手动签到</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label=" 签退方式" prop="signOutMethod">
          <el-radio-group v-model="form.signOutMethod">
            <el-radio :label="0">人脸签退</el-radio>
            <el-radio :label="1">扫码签退</el-radio>
            <el-radio :label="2">手动签退</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
  import { listAttendance, getAttendance, delAttendance, addAttendance, updateAttendance } from "@/api/meeting/attendance";

  export default {
    name: "Attendance",
    data() {
      return {
        //日期范围查询
        dateRange: [],
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 签到考勤表格数据
        attendanceList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          userId: null,
          userName: null,
          meetingId: null,
          meetingName: null,
          isLeave: null,
          signInStatus: null,
          signInMethod: null,
          signInTime: null,
          signOutStatus: null,
          signOutMethod: null,
          signOutTime: null,
          isDel: null,
        },
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          meetingId: [
            { required: true, message: "会议编号不能为空", trigger: "blur" }
          ],
          meetingName: [
            { required: true, message: "会议名称不能为空", trigger: "blur" }
          ],
          isLeave: [
            { required: true, message: "是否请假 0否 1请假不能为空", trigger: "blur" }
          ],
          signInStatus: [
            { required: true, message: "签到状态 0未签到 1已签到 2迟到不能为空", trigger: "change" }
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
        opetionIsLeave: [
          {
            label: "否",
            value: "0"
          },
          {
            label: "是",
            value: '1'
          }
        ],
        opetionSignInStatus: [
          {
            label: "未签到",
            value: "0"
          },
          {
            label: "已签到",
            value: '1'
          },
          {
            label: "迟到",
            value: '2'
          },
          {
            label: "补签",
            value: '3'
          }
        ],
        opetionSignInMethod: [
          {
            label: "人脸",
            value: 0
          },
          {
            label: "扫码",
            value: 1
          },
          {
            label: "手动",
            value: 2
          }

        ]
      };
    },
    created() {
      this.getList();
    },
    methods: {
      /** 查询签到考勤列表 */
      getList() {
        this.loading = true;
        let params = this.addDateRange(this.queryParams, this.dateRange);
        console.log("查询参数", params);
        listAttendance(params).then(response => {
          this.attendanceList = response.rows;
          this.total = response.total;
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
          id: null,
          userId: null,
          userName: null,
          meetingId: null,
          meetingName: null,
          isLeave: null,
          signInStatus: null,
          signInMethod: null,
          signInTime: null,
          signOutStatus: null,
          signOutMethod: null,
          signOutTime: null,
          isDel: null,
          createTime: null,
          updateTime: null
        };
        this.resetForm("form");
      },
      /** 搜索按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.dateRange = [];
        this.resetForm("queryForm");
        this.getList();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.id)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加签到考勤";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const id = row.id || this.ids
        getAttendance(id).then(response => {
          this.form = response.data;
          this.open = true;
          this.title = "修改签到考勤";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            if (this.form.id != null) {
              updateAttendance(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addAttendance(this.form).then(response => {
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
        const ids = row.id || this.ids;
        this.$modal.confirm('是否确认删除签到考勤编号为"' + ids + '"的数据项？').then(function () {
          return delAttendance(ids);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('/meeting/attendance/export', {
          ...this.queryParams
        }, `attendance_${new Date().getTime()}.xlsx`)
      },
      // 返回状态标签文本
      getSignInStatusLabel(status) {
        switch (status) {
          case '0': return '未签到';
          case '1': return '已签到';
          case '2': return '迟到';
          case '3': return '补签';

        }
      },
      // 返回对应的颜色类型
      getSignInStatusType(status) {
        switch (status) {
          case '0': return 'warning';  // 黄色
          case '1': return 'success';  // 黄色
          case '2': return 'danger';  // 绿色

        }
      },
      getSignOutStatusLabel(status) {
        switch (status) {
          case '0': return '未签退';
          case '1': return '已签退';
          case '2': return '早退';
          case '3': return '补签';
        }
      },
      getSignOutStatusType(status) {
        switch (status) {
          case '0': return 'warning';  // 黄色
          case '1': return 'success';  // 黄色
          case '2': return 'danger';  // 绿色
        }
      },

      getSignInMethodLabel(method) {
        switch (method) {
          case 0: return '人脸签到';
          case 1: return '二维码签到';
          case 2: return '手动签到';
          default: return '暂未签到'
        }
      },
      getSignOutMethodLabel(method) {
        switch (method) {
          case 0: return '人脸签退';
          case 1: return '二维码签退';
          case 2: return '手动签退';
          default: return '暂未签退'
        }
      },

      getIsLeaveLabel(leave) {
        switch (leave) {
          case '0': return '否';
          case '1': return '是';
        }
      }
    }
  };
</script>