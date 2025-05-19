<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="82px">
      <el-form-item label="会议室名称" prop="roomName">
        <el-input v-model="queryParams.roomName" placeholder="请输入会议室名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="位置信息" prop="location">
        <el-input v-model="queryParams.location" placeholder="请输入位置信息" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="容纳人数" prop="capacity">
        <el-input v-model="queryParams.capacity" placeholder="请输入容纳人数" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="状态" clearable>
          <el-option v-for="dict in opetionStatus " :key="dict.value" :label="dict.label" :value="dict.value" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" round size="mini" @click="handleQuery">查询</el-button>
        <el-button icon="el-icon-refresh" size="mini" round @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['meeting:room:add']">新增会议室</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['meeting:room:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['meeting:room:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['meeting:room:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="roomList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="序号" align="center" width="120">
        <template slot-scope="scope">
          {{ scope.$index + 1 + (queryParams.pageNum - 1) * queryParams.pageSize }}
        </template>
      </el-table-column>
      <el-table-column label="会议室名称" show-overflow-tooltip align="center" prop="roomName" />
      <el-table-column label="位置信息" show-overflow-tooltip align="center" prop="location" />
      <el-table-column label="容纳人数" align="center" prop="capacity" />
      <el-table-column label="设备信息" align="center" prop="equipments">
        <template slot-scope="scope">
          <!-- 自定义列内容，点击时查询设备信息 -->
          <el-button type="text" @click="handleQueryEquipment(scope.row)">查询设备</el-button>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createTime" width="150" />
      <el-table-column label="状态" align="center" prop="status">
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
            v-hasPermi="['meeting:room:edit']">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:room:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />


    <!-- 会议室设备详情弹窗 -->
    <el-dialog title="设备详情" :visible.sync="dialogVisible" width="50%" style="margin-top: 100px;"
      :modal-append-to-body="false" :body-style="{ maxHeight: '60vh', overflowY: 'auto' }">
      <!-- 设备表格 -->
      <el-table :data="queryResult" empty-text="暂无设备数据" border style="width: 100%">
        <el-table-column prop="equipmentName" label="设备名称" align="center" />
        <el-table-column prop="modelNumber" label="设备型号" align="center" />
        <el-table-column label="设备图片" prop="imagePath" align="center">
          <template slot-scope="scope">
            <image-preview :src="scope.row.imagePath" :width="50" :height="50" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注信息" align="center" />
      </el-table>

      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
      </span>
    </el-dialog>






    <!-- 添加或修改会议室信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="会议室名称" prop="roomName">
          <el-input v-model="form.roomName" placeholder="请输入会议室名称" />
        </el-form-item>
        <el-form-item label="位置信息" prop="location">
          <el-input v-model="form.location" placeholder="请输入位置信息" />
        </el-form-item>
        <el-form-item label="容纳人数" prop="capacity">
          <el-input v-model="form.capacity" placeholder="请输入容纳人数" />
        </el-form-item>
        <el-form-item label="岗位状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in opetionRoomStatus" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="设备" prop="equipment">
          <el-select v-model="form.equipmentIds" multiple placeholder="请选择设备">
            <el-option v-for="item in equipmentOptions" :key="item.equipmentId" :label="item.equipmentName"
              :value="item.equipmentId" :disabled="item.status == 1"></el-option>
          </el-select>
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
  import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect } from "@/api/system/user";
  import { listRoom, getRoom, delRoom, addRoom, updateRoom } from "@/api/meeting/room";
  import { listEquipment, getEquipment, delEquipment, addEquipment, updateEquipment, changeEquipmentStatus } from "@/api/meeting/equipment";
  import { getDept } from '@/api/system/dept'
  import Treeselect from "@riophae/vue-treeselect";
  import "@riophae/vue-treeselect/dist/vue-treeselect.css";
  export default {
    name: "Room",
    components: { Treeselect },
    data() {
      return {
        // 过滤掉已禁用部门树选项
        enabledDeptOptions: undefined,
        // 所有部门树选项
        deptOptions: undefined,
        equipmentOptions: [],
        equipmentList: [],
        equipmentDialogVisible: false,
        queryResult: [],
        dialogVisible: false,
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 子表选中数据
        checkedMeetingRoomEquipment: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示搜索条件
        showSearch: true,
        // 总条数
        total: 0,
        // 会议室信息表格数据
        roomList: [],
        // 会议室和设备关联表格数据
        meetingRoomEquipmentList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          deptId: null,
          pageNum: 1,
          pageSize: 10,
          deptName: null,
          roomName: null,
          location: null,
          capacity: null,
          status: null,
        },
        // 表单参数
        form: {

        },
        opetionStatus: [
          {
            label: "正常",
            value: "0"
          },
          {
            label: "停用",
            value: '1'
          }
        ],
        opetionRoomStatus: [
          {
            label: "正常",
            value: "0"
          },
          {
            label: "停用",
            value: '1'
          }
        ],
        // 表单校验
        rules: {
          roomName: [
            { required: true, message: "会议室名称不能为空", trigger: "blur" }
          ],
          capacity: [
            { required: true, message: "容纳人数不能为空", trigger: "blur" }
          ],
          deptId: [
            { required: true, message: "所属部门不能为空", trigger: "blur" }
          ],
        },
      };
    },
    created() {
      this.getList();
      // this.getDeptTree();
    },
    methods: {


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


      // 点击查询设备时的处理方法
      handleQueryEquipment(row) {
        this.dialogVisible = true; // 显示弹窗
        getRoom(row.roomId).then(result => {
          console.log("设备信息： " + result);

          this.queryResult = result.data.equipments || []; // 设置查询结果

        }).catch(error => {
          this.queryResult = []; // 异常时重置为空数组 
        });
      },


      // // 设备状态修改
      // handleStatusChange(row) {
      //   let text = row.status === "0" ? "启用" : "停用";
      //   this.$modal.confirm('确认要"' + text + '""' + row.roomName + '"会议室吗？').then(function () {
      //     return changeRoomStatus(row.roomId, row.status);
      //   }).then(() => {
      //     this.$modal.msgSuccess(text + "成功");
      //   }).catch(function () {
      //     row.status = row.status === "0" ? "1" : "0";
      //   });
      // },

      /** 查询会议室信息列表 */
      getList() {
        this.loading = true;

        listRoom(this.queryParams).then(response => {
          this.roomList = response.rows;
          this.total = response.total;
          this.loading = false;
        });
        console.log(this.roomList);


      },
      // 取消按钮
      cancel() {
        this.open = false;
        this.reset();
      },
      // 表单重置
      reset() {
        this.form = {
          roomId: null,
          roomName: null,
          location: null,
          capacity: null,
          equipment: null,
          deptId: null,
          status: null,
          createBy: null,
          createTime: null,
          updateBy: null,
          updateTime: null
        };
        this.sysDeptList = [];
        this.resetForm("form");
      },
      /** 查询按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.roomId)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        getRoom().then(response => {
          this.open = true;
          this.equipmentOptions = response.equipments;
          this.title = "添加会议室信息";
        })
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const roomId = row.roomId || this.ids
        getRoom(roomId).then(response => {
          console.log("修改会议室信息：", response);

          this.form = response.data;
          this.equipmentOptions = response.equipments;
          this.$set(this.form, "equipmentIds", response.equipmentIds);
          this.open = true;
          this.title = "修改会议室信息";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            this.form.meetingRoomEquipmentList = this.meetingRoomEquipmentList;
            if (this.form.roomId != null) {
              updateRoom(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addRoom(this.form).then(response => {
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
        const roomIds = row.roomId || this.ids;
        this.$modal.confirm('是否确认删除会议室信息编号为"' + roomIds + '"的数据项？').then(function () {
          return delRoom(roomIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 会议室和设备关联序号 */
      rowMeetingRoomEquipmentIndex({ row, rowIndex }) {
        row.index = rowIndex + 1;
      },
      /** 会议室和设备关联添加按钮操作 */
      handleAddMeetingRoomEquipment() {
        let obj = {};
        obj.equipmentId = "";
        this.meetingRoomEquipmentList.push(obj);
      },
      /** 会议室和设备关联删除按钮操作 */
      handleDeleteMeetingRoomEquipment() {
        if (this.checkedMeetingRoomEquipment.length == 0) {
          this.$modal.msgError("请先选择要删除的会议室和设备关联数据");
        } else {
          const meetingRoomEquipmentList = this.meetingRoomEquipmentList;
          const checkedMeetingRoomEquipment = this.checkedMeetingRoomEquipment;
          this.meetingRoomEquipmentList = meetingRoomEquipmentList.filter(function (item) {
            return checkedMeetingRoomEquipment.indexOf(item.index) == -1
          });
        }
      },
      /** 复选框选中数据 */
      handleMeetingRoomEquipmentSelectionChange(selection) {
        this.checkedMeetingRoomEquipment = selection.map(item => item.index)
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('meeting/room/export', {
          ...this.queryParams
        }, `room_${new Date().getTime()}.xlsx`)
      },
      // 返回状态标签文本
      getStatusLabel(status) {
        switch (status) {
          case '0': return '正常';
          case '1': return '停用';

        }
      },
      // 返回对应的颜色类型
      getStatusType(status) {
        switch (status) {
          case '1': return 'danger';  // 绿色

        }
      }
    }
  };
</script>