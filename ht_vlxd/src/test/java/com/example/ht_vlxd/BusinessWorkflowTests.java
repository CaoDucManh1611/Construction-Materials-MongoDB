package com.example.ht_vlxd;

import com.example.ht_vlxd.Model.auth.*;
import com.example.ht_vlxd.Model.customer.*;
import com.example.ht_vlxd.Model.sales.*;
import com.example.ht_vlxd.Model.product.*;
import com.example.ht_vlxd.Model.inventory.*;
import com.example.ht_vlxd.Model.finance.*;
import com.example.ht_vlxd.Repository.auth.*;
import com.example.ht_vlxd.Repository.customer.*;
import com.example.ht_vlxd.Repository.sales.*;
import com.example.ht_vlxd.Repository.product.*;
import com.example.ht_vlxd.Repository.inventory.*;
import com.example.ht_vlxd.Repository.finance.*;
import com.example.ht_vlxd.Service.sales.BusinessWorkflowService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class BusinessWorkflowTests {
    @Autowired BusinessWorkflowService workflow;
    @Autowired DonHangRepository orders;
    @Autowired HopDongRepository contracts;
    @Autowired CongNoRepository debts;
    @Autowired ThanhToanRepository payments;
    @Autowired PhieuKhoRepository slips;
    @Autowired GiaoNhanRepository deliveries;
    @Autowired TonKhoRepository stock;
    @Autowired HangHoaRepository products;
    @Autowired KhoRepository warehouses;
    @Autowired NguoiDungRepository users;
    @Autowired KhachHangRepository customers;
    @Autowired DoiTraHangRepository returns;
    @Autowired MongoTemplate mongo;
    @Autowired com.example.ht_vlxd.Repository.supplier.NhaCungCapRepository suppliers;
    @Autowired com.example.ht_vlxd.Service.product.CatalogApprovalService catalog;
    DonHang order;
    HopDong contract;
    HangHoa product;
    Kho warehouse;
    @BeforeEach void fixture() {
        assertTrue(mongo.getDb().getName().startsWith("vlxd_test"), "Tests must use an isolated test database");
        login("giamdoc", "BAN_QUAN_LY");
        warehouse = warehouses.findAll().getFirst();
        product = new HangHoa(); product.setMaHang(BusinessWorkflowService.code("TEST")); product.setTenHang("Test vật liệu");
        product.setDonViTinh("bao"); product.setGiaBanLe(new BigDecimal("100")); product.setTrangThai(TrangThaiHangHoa.KINH_DOANH); products.save(product);
        stock.save(new TonKho(null, product, warehouse, new BigDecimal("20")));
        order = new DonHang(); order.setMaDonHang(BusinessWorkflowService.code("TEST-DH"));
        order.setKhachHang(customers.findByNguoiDungUsername("khachhang01")); order.setTongTien(new BigDecimal("1000"));
        var line = new DonHangChiTiet(); line.setHangHoa(product); line.setSoLuong(BigDecimal.TEN); line.setDonGia(new BigDecimal("100")); line.setThanhTien(new BigDecimal("1000"));
        order.setChiTiet(new ArrayList<>(List.of(line))); orders.save(order);
        workflow.approveOrder(order.getMaDonHang()); order = orders.findById(order.getId()).orElseThrow();
        contract = new HopDong(); contract.setMaHopDong(BusinessWorkflowService.code("TEST-HD")); contract.setDonHang(order); contract.setKhachHang(order.getKhachHang());
        contract.setGiaTri(new BigDecimal("1000")); contract.setTienDatCoc(new BigDecimal("300")); contract.setNgayHieuLuc(LocalDate.now()); contract.setChiTiet(List.of(line)); contracts.save(contract);
    }
    @AfterEach void clearAuth() { SecurityContextHolder.clearContext(); }
    void login(String username, String role) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(username, null, AuthorityUtils.createAuthorityList("ROLE_" + role)));
    }
    CongNo signedDebt() {
        workflow.approveContract(contract.getMaHopDong()); login("nvkd01", "NV_KINH_DOANH"); workflow.signContract(contract.getMaHopDong());
        return debts.findByDonHangId(order.getId());
    }
    PhieuKho exportSlip() {
        var slip = new PhieuKho(); slip.setMaPhieu(BusinessWorkflowService.code("TEST-PX")); slip.setLoaiPhieu("XUAT"); slip.setKho(warehouse); slip.setDonHang(orders.findById(order.getId()).orElseThrow());
        var line = new PhieuKhoChiTiet(); line.setHangHoa(product); line.setSoLuong(BigDecimal.TEN); line.setDonGia(BigDecimal.ZERO); slip.setChiTiet(List.of(line)); return slips.save(slip);
    }
    @Test void depositMustBeActuallyCollectedAndExportCannotRepeat() {
        assertThrows(IllegalArgumentException.class, () -> workflow.signContract(contract.getMaHopDong()));
        var debt = signedDebt(); assertEquals(0, debt.getSoTienDaTt().signum());
        var slip = exportSlip(); assertThrows(IllegalArgumentException.class, () -> workflow.approveSlip(slip.getMaPhieu()));
        login("nvkt01", "NV_KE_TOAN");
        assertThrows(IllegalArgumentException.class, () -> workflow.collect(debt.getId(), BigDecimal.TEN.negate(), "TIEN_MAT", "negative", ""));
        String tx = UUID.randomUUID().toString(); workflow.collect(debt.getId(), new BigDecimal("300"), "CHUYEN_KHOAN", tx, "");
        workflow.collect(debt.getId(), new BigDecimal("300"), "CHUYEN_KHOAN", tx, "");
        assertEquals(1, payments.findByCongNoId(debt.getId()).size());
        assertEquals(0, debts.findById(debt.getId()).orElseThrow().getSoTienDaTt().compareTo(new BigDecimal("300")));
        login("giamdoc", "BAN_QUAN_LY"); workflow.approveSlip(slip.getMaPhieu());
        assertThrows(IllegalArgumentException.class, () -> workflow.approveSlip(slip.getMaPhieu()));
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(BigDecimal.TEN));
        var delivery = new GiaoNhan(); delivery.setMaGiaoNhan(BusinessWorkflowService.code("TEST-GN")); delivery.setDonHang(orders.findById(order.getId()).orElseThrow()); delivery.setTrangThai("DANG_GIAO"); deliveries.save(delivery);
        var exported = orders.findById(order.getId()).orElseThrow(); exported.setTrangThai("DANG_GIAO"); orders.save(exported);
        workflow.handover(delivery.getMaGiaoNhan(), "OK", "Đã kiểm tra và nhận đủ");
        assertThrows(IllegalArgumentException.class, () -> workflow.handover(delivery.getMaGiaoNhan(), "OK", "repeat"));
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(BigDecimal.TEN));
    }
    @Test void insufficientStockRollsBackPaymentAndExportState() {
        var second = new HangHoa(); second.setMaHang(BusinessWorkflowService.code("TEST-SECOND")); second.setTenHang("Hàng thiếu ở dòng thứ hai"); second.setGiaBanLe(new BigDecimal("50")); products.save(second);
        stock.save(new TonKho(null, second, warehouse, BigDecimal.ONE));
        var detail = new DonHangChiTiet(); detail.setHangHoa(second); detail.setSoLuong(new BigDecimal("2")); detail.setDonGia(new BigDecimal("50")); detail.setThanhTien(new BigDecimal("100"));
        order.getChiTiet().add(detail); order.setTongTien(new BigDecimal("1100")); orders.save(order);
        contract.setChiTiet(new ArrayList<>(order.getChiTiet())); contract.setGiaTri(new BigDecimal("1100")); contracts.save(contract);
        var debt = signedDebt(); login("nvkt01", "NV_KE_TOAN"); workflow.collect(debt.getId(), new BigDecimal("300"), "TIEN_MAT", UUID.randomUUID().toString(), "");
        var slip = exportSlip(); var secondLine = new PhieuKhoChiTiet(); secondLine.setHangHoa(second); secondLine.setSoLuong(new BigDecimal("2")); secondLine.setDonGia(BigDecimal.ZERO);
        slip.setChiTiet(new ArrayList<>(slip.getChiTiet())); slip.getChiTiet().add(secondLine); slips.save(slip); login("giamdoc", "BAN_QUAN_LY");
        assertThrows(IllegalArgumentException.class, () -> workflow.approveSlip(slip.getMaPhieu()));
        assertEquals("NHAP", slips.findById(slip.getId()).orElseThrow().getTrangThai());
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(new BigDecimal("20")));
        assertEquals(0, stock.findByHangHoaIdAndKhoId(second.getId(), warehouse.getId()).getSoLuong().compareTo(BigDecimal.ONE));
        assertEquals("DA_XAC_NHAN", orders.findById(order.getId()).orElseThrow().getTrangThai());
    }
    @Test void damagedReturnUsesContractPriceAndDoesNotEnterSaleableStock() {
        var debt = signedDebt(); login("nvkt01", "NV_KE_TOAN"); workflow.collect(debt.getId(), new BigDecimal("1000"), "TIEN_MAT", UUID.randomUUID().toString(), "");
        var slip = exportSlip(); login("giamdoc", "BAN_QUAN_LY"); workflow.approveSlip(slip.getMaPhieu());
        product.setGiaBanLe(new BigDecimal("999")); products.save(product);
        var request = new DoiTraHang(); request.setMaDoiTra(BusinessWorkflowService.code("TEST-DT")); request.setDonHang(orders.findById(order.getId()).orElseThrow()); request.setKhachHang(order.getKhachHang()); request.setHangHoa(product); request.setSoLuong(BigDecimal.ONE); request.setLoai("TRA"); returns.save(request);
        workflow.approveReturn(request.getMaDoiTra(), true); workflow.receiveReturn(request.getMaDoiTra(), warehouse.getId(), "HONG", "Hàng vỡ, cách ly");
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(BigDecimal.TEN));
        var adjusted = debts.findById(debt.getId()).orElseThrow(); assertEquals(0, adjusted.getSoTienNo().compareTo(new BigDecimal("900")));
        assertEquals(0, adjusted.getSoTienDaTt().compareTo(new BigDecimal("1000"))); assertEquals(0, adjusted.getSoTienHoanTra().compareTo(new BigDecimal("100")));
        assertThrows(IllegalArgumentException.class, () -> workflow.receiveReturn(request.getMaDoiTra(), warehouse.getId(), "HONG", "repeat"));
        login("nvkt01", "NV_KE_TOAN");
        String tx = UUID.randomUUID().toString();
        workflow.refund(debt.getId(), new BigDecimal("100"), tx);
        workflow.refund(debt.getId(), new BigDecimal("100"), tx);
        assertEquals(0, debts.findById(debt.getId()).orElseThrow().getSoTienHoanTra().signum());
        assertEquals(1, payments.findByCongNoId(debt.getId()).stream().filter(p -> "CHI_HOAN".equals(p.getLoaiPhieu())).count());
    }
    @Test void importInspectionCreatesSupplierPayableAndCannotRepeat() {
        var slip = new PhieuKho(); slip.setMaPhieu(BusinessWorkflowService.code("TEST-PN")); slip.setLoaiPhieu("NHAP");
        var supplier = new com.example.ht_vlxd.Model.supplier.NhaCungCap(); supplier.setMaNcc(BusinessWorkflowService.code("TEST-NCC")); supplier.setTenNcc("NCC kiểm thử"); suppliers.save(supplier);
        slip.setKho(warehouse); slip.setNhaCungCap(supplier);
        var line = new PhieuKhoChiTiet(); line.setHangHoa(product); line.setSoLuong(new BigDecimal("2")); line.setDonGia(new BigDecimal("75"));
        slip.setChiTiet(List.of(line)); slips.save(slip);
        assertThrows(IllegalArgumentException.class, () -> workflow.approveSlip(slip.getMaPhieu()));
        slip.setBienBanNhapHang("Kiểm đếm đủ 2 bao, nguyên vẹn"); slips.save(slip);
        workflow.approveSlip(slip.getMaPhieu());
        assertThrows(IllegalArgumentException.class, () -> workflow.approveSlip(slip.getMaPhieu()));
        var debt = mongo.findOne(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where("phieuNhapId").is(slip.getId())), CongNo.class);
        assertNotNull(debt); assertEquals("PHAI_TRA", debt.getLoaiCongNo()); assertEquals(0, debt.getSoTienNo().compareTo(new BigDecimal("150")));
        login("nvkt01", "NV_KE_TOAN"); workflow.collect(debt.getId(), new BigDecimal("150"), "CHUYEN_KHOAN", UUID.randomUUID().toString(), "Trả NCC");
        assertEquals("CHI", payments.findByCongNoId(debt.getId()).getFirst().getLoaiPhieu());
    }
    @Test void exchangeCreatesReplacementReleaseWithoutDeductingStockAtReceipt() {
        var debt = signedDebt(); login("nvkt01", "NV_KE_TOAN"); workflow.collect(debt.getId(), new BigDecimal("300"), "TIEN_MAT", UUID.randomUUID().toString(), "");
        var slip = exportSlip(); login("giamdoc", "BAN_QUAN_LY"); workflow.approveSlip(slip.getMaPhieu());
        var request = new DoiTraHang(); request.setMaDoiTra(BusinessWorkflowService.code("TEST-DOI")); request.setDonHang(orders.findById(order.getId()).orElseThrow()); request.setKhachHang(order.getKhachHang()); request.setHangHoa(product); request.setSoLuong(BigDecimal.ONE); request.setLoai("DOI"); returns.save(request);
        workflow.approveReturn(request.getMaDoiTra(), true); workflow.receiveReturn(request.getMaDoiTra(), warehouse.getId(), "HONG", "Thu hồi bao hỏng");
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(BigDecimal.TEN));
        var replacement = mongo.findOne(org.springframework.data.mongodb.core.query.Query.query(org.springframework.data.mongodb.core.query.Criteria.where("doiTraId").is(request.getId())), DonHang.class);
        assertNotNull(replacement); assertEquals(0, replacement.getTongTien().signum());
        var next = new PhieuKho(); next.setMaPhieu(BusinessWorkflowService.code("TEST-PX-DOI")); next.setLoaiPhieu("XUAT"); next.setKho(warehouse); next.setDonHang(replacement);
        var line = new PhieuKhoChiTiet(); line.setHangHoa(product); line.setSoLuong(BigDecimal.ONE); line.setDonGia(BigDecimal.ZERO); next.setChiTiet(List.of(line)); slips.save(next);
        workflow.approveSlip(next.getMaPhieu());
        assertEquals(0, stock.findByHangHoaIdAndKhoId(product.getId(), warehouse.getId()).getSoLuong().compareTo(new BigDecimal("9")));
        assertEquals("CHO_GIAO_DOI", returns.findById(request.getId()).orElseThrow().getTrangThai());
    }
    @Test void catalogProposalRequiresManagerDecisionAndRetainsHistory() {
        login("nvkd01", "NV_KINH_DOANH"); catalog.proposeDisable("HANG_HOA", product.getId()); catalog.proposeDisable("HANG_HOA", product.getId());
        assertEquals(TrangThaiHangHoa.KINH_DOANH, products.findById(product.getId()).orElseThrow().getTrangThai());
        var proposal = catalog.pending().stream().filter(p -> product.getId().equals(p.getDoiTuongId())).findFirst().orElseThrow();
        login("giamdoc", "BAN_QUAN_LY"); catalog.decide(proposal.getId(), true);
        assertEquals(TrangThaiHangHoa.NGUNG_KINH_DOANH, products.findById(product.getId()).orElseThrow().getTrangThai());
        assertNotNull(orders.findById(order.getId()).orElseThrow().getChiTiet().getFirst().getHangHoa());
        assertThrows(IllegalArgumentException.class, () -> catalog.decide(proposal.getId(), true));
    }
    @Test void repeatedPriceEditProposalsDoNotChangeContractSnapshots() {
        product.setDanhMuc(mongo.findAll(DanhMuc.class).getFirst()); product.setGiaBanLe(new BigDecimal("150"));
        login("nvkd01", "NV_KINH_DOANH"); catalog.proposeProduct(product); catalog.proposeProduct(product);
        assertEquals(0, products.findById(product.getId()).orElseThrow().getGiaBanLe().compareTo(new BigDecimal("100")));
        var proposal = catalog.pending().stream().filter(p -> product.getId().equals(p.getDoiTuongId())).findFirst().orElseThrow();
        login("giamdoc", "BAN_QUAN_LY"); catalog.decide(proposal.getId(), true);
        assertEquals(0, products.findById(product.getId()).orElseThrow().getGiaBanLe().compareTo(new BigDecimal("150")));
        assertEquals(0, contracts.findById(contract.getId()).orElseThrow().getChiTiet().getFirst().getDonGia().compareTo(new BigDecimal("100")));
    }
    @Test void unpaidContractCancellationPreservesDocumentsAndAllowsOrderCancellation() {
        var debt = signedDebt(); workflow.cancelContract(contract.getMaHopDong()); workflow.cancelOrder(order.getMaDonHang(), false);
        assertEquals("DA_HUY", orders.findById(order.getId()).orElseThrow().getTrangThai());
        assertEquals("DA_HUY", contracts.findById(contract.getId()).orElseThrow().getTrangThai());
        assertEquals("DA_HUY", debts.findById(debt.getId()).orElseThrow().getTrangThai());
    }
}
