<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="82px">

      <el-form-item label="设备名称" prop="equipmentName">
        <el-input v-model="queryParams.equipmentName" placeholder="请输入设备名称" clearable
          @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="设备型号" prop="modelNumber">
        <el-input v-model="queryParams.modelNumber" placeholder="请输入设备型号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="采购日期" prop="purchaseDate">
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
        <el-button type="primary" plain round icon="el-icon-plus" size="mini" @click="handleAdd"
          v-hasPermi="['meeting/equipment:equipment:add']">新增设备</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain round icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate"
          v-hasPermi="['meeting/equipment:equipment:edit']">修改</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" plain round icon="el-icon-delete" size="mini" :disabled="multiple"
          @click="handleDelete" v-hasPermi="['meeting/equipment:equipment:remove']">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="primary" round plain icon="el-icon-download" size="mini" @click="handleExport"
          v-hasPermi="['meeting/equipment:equipment:export']">导出</el-button>
      </el-col>
    </el-row>

    <el-table v-loading="loading" :data="equipmentList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="设备编号" align="center" prop="equipmentId" />
      <el-table-column label="设备名称" align="center" prop="equipmentName" />
      <el-table-column label="设备型号" align="center" prop="modelNumber" />
      <el-table-column label="设备图片" align="center" prop="imagePath" width="100">
        <template slot-scope="scope">
          <image-preview :src="scope.row.imagePath" :width="50" :height="50" />
        </template>
      </el-table-column>
      <el-table-column label="采购日期" align="center" prop="purchaseDate" width="180">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.purchaseDate) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="备注信息" align="center" prop="remark" />
      <el-table-column label="设备状态" align="center" prop="status">
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
            v-hasPermi="['meeting:equipment:edit']">修改</el-button>
          <el-button size="mini" type="text" @click="handleDelete(scope.row)"
            v-hasPermi="['meeting:equipment:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize"
      @pagination="getList" />

    <!-- 添加或修改会议室设备信息对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="550px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备名称" prop="equipmentName">
              <el-input v-model="form.equipmentName" placeholder="请输入设备名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备型号" prop="modelNumber">
              <el-input v-model="form.modelNumber" placeholder="请输入设备型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="设备图片" prop="imagePath">
          <image-upload v-model="form.imagePath" />
        </el-form-item>
        <el-form-item label="岗位状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio v-for="dict in opetionStatus" :key="dict.value" :label="dict.value">{{dict.label}}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注信息" prop="remark">
          <el-input v-model="form.remark" type="textarea" placeholder="请输入备注信息" />
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
  import { listEquipment, getEquipment, delEquipment, addEquipment, updateEquipment, changeEquipmentStatus } from "@/api/meeting/equipment";

  export default {
    name: "Equipment",
    data() {
      return {
        //日期範圍
        dateRange: [],
        // 遮罩层
        loading: true,
        // 选中数组
        ids: [],
        // 子表选中数据
        checkedMeetingRoom: [],
        // 非单个禁用
        single: true,
        // 非多个禁用
        multiple: true,
        // 显示查询条件
        showSearch: true,
        // 总条数
        total: 0,
        // 会议室设备信息表格数据
        equipmentList: [],
        // 会议室信息表格数据
        meetingRoomList: [],
        // 弹出层标题
        title: "",
        // 是否显示弹出层
        open: false,
        // 查询参数
        queryParams: {
          pageNum: 1,
          pageSize: 10,
          roomId: null,
          equipmentName: null,
          modelNumber: null,
          status: null,
          imagePath: null,
          purchaseDate: null,
        },
        opetionStatus: [
          {
            label: "正常",
            value: '0'
          },
          {
            label: "停用",
            value: '1'
          }
        ],
        // 表单参数
        form: {},
        // 表单校验
        rules: {
          roomId: [
            { required: true, message: "会议室编号不能为空", trigger: "blur" }
          ],
          modelNumber: [
            { required: true, message: "设备型号不能为空", trigger: "blur" }
          ],
          equipmentName: [
            { required: true, message: "设备名称不能为空", trigger: "blur" }
          ],
          status: [
            { required: true, message: "设备状态不能为空", trigger: "change" }
          ],
          createBy: [
            { required: true, message: "创建者不能为空", trigger: "blur" }
          ],
          createTime: [
            { required: true, message: "创建时间不能为空", trigger: "blur" }
          ],
        },
        pickerOptions: {
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
          disabledDate(time) {
            // 限制只能选择今天之后的日期
            return time.getTime() < Date.now() - 8.64e7; // 8.64e7 是一天的毫秒数
          }
        }
      };
    },
    created() {
      this.getList();
    },
    methods: {

      // // 设备状态修改
      // handleStatusChange(row) {
      //   let text = row.status === "0" ? "启用" : "停用";
      //   this.$modal.confirm('确认要"' + text + '""' + row.equipmentName + '"设备吗？').then(function () {
      //     return changeEquipmentStatus(row.equipmentId, row.status);
      //   }).then(() => {
      //     this.$modal.msgSuccess(text + "成功");
      //   }).catch(function () {
      //     row.status = row.status === "0" ? "1" : "0";
      //   });
      // },

      /** 查询会议室设备信息列表 */
      getList() {
        this.loading = true;
        console.log("Date Range:", this.dateRange);
        console.log("Query Params:", this.addDateRange(this.queryParams, this.dateRange));
        listEquipment(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
          this.equipmentList = response.rows;
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
          equipmentId: null,
          roomId: null,
          equipmentName: null,
          modelNumber: null,
          status: null,
          imagePath: null,
          purchaseDate: null,
          remark: null,
          createBy: null,
          createTime: null,
          updateBy: null,
          updateTime: null
        };
        this.meetingRoomList = [];
        this.resetForm("form");
      },
      /** 查询按钮操作 */
      handleQuery() {
        this.queryParams.pageNum = 1;
        this.getList();
      },
      /** 重置按钮操作 */
      resetQuery() {
        this.dateRange = [];
        this.resetForm("queryForm");
        this.handleQuery();
      },
      // 多选框选中数据
      handleSelectionChange(selection) {
        this.ids = selection.map(item => item.equipmentId)
        this.single = selection.length !== 1
        this.multiple = !selection.length
      },
      /** 新增按钮操作 */
      handleAdd() {
        this.reset();
        this.open = true;
        this.title = "添加会议室设备";
      },
      /** 修改按钮操作 */
      handleUpdate(row) {
        this.reset();
        const equipmentId = row.equipmentId || this.ids
        getEquipment(equipmentId).then(response => {
          this.form = response.data;
          this.meetingRoomList = response.data.meetingRoomList;
          this.open = true;
          this.title = "修改会议室设备";
        });
      },
      /** 提交按钮 */
      submitForm() {
        this.$refs["form"].validate(valid => {
          if (valid) {
            this.form.meetingRoomList = this.meetingRoomList;
            if (this.form.equipmentId != null) {
              updateEquipment(this.form).then(response => {
                this.$modal.msgSuccess("修改成功");
                this.open = false;
                this.getList();
              });
            } else {
              addEquipment(this.form).then(response => {
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
        const equipmentIds = row.equipmentId || this.ids;
        this.$modal.confirm('是否确认删除会议室设备信息编号为"' + equipmentIds + '"的数据项？').then(function () {
          return delEquipment(equipmentIds);
        }).then(() => {
          this.getList();
          this.$modal.msgSuccess("删除成功");
        }).catch(() => { });
      },
      /** 会议室信息序号 */
      rowMeetingRoomIndex({ row, rowIndex }) {
        row.index = rowIndex + 1;
      },
      /** 会议室信息添加按钮操作 */
      handleAddMeetingRoom() {
        let obj = {};
        obj.roomName = "";
        obj.location = "";
        obj.capacity = "";
        obj.equipment = "";
        obj.deptId = "";
        obj.status = "";
        this.meetingRoomList.push(obj);
      },
      /** 会议室信息删除按钮操作 */
      handleDeleteMeetingRoom() {
        if (this.checkedMeetingRoom.length == 0) {
          this.$modal.msgError("请先选择要删除的会议室信息数据");
        } else {
          const meetingRoomList = this.meetingRoomList;
          const checkedMeetingRoom = this.checkedMeetingRoom;
          this.meetingRoomList = meetingRoomList.filter(function (item) {
            return checkedMeetingRoom.indexOf(item.index) == -1
          });
        }
      },
      /** 复选框选中数据 */
      handleMeetingRoomSelectionChange(selection) {
        this.checkedMeetingRoom = selection.map(item => item.index)
      },
      /** 导出按钮操作 */
      handleExport() {
        this.download('meeting/equipment/export', {
          ...this.queryParams
        }, `equipment_${new Date().getTime()}.xlsx`)
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