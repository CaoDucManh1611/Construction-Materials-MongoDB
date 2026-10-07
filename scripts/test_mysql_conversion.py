from convert_mysql_export import convert
from decimal import Decimal
import unittest
class ConversionTests(unittest.TestCase):
    def test_ids_embedded_details_and_precision(self):
        result = convert({'don_hang': [{'id': 4, 'tong_tien': Decimal('123.45'), 'khach_hang_id': 7}],
            'don_hang_chi_tiet': [{'id': 9, 'don_hang_id': 4, 'hang_hoa_id': 3, 'so_luong': Decimal('1.125')} ]})
        order = result['don_hang'][0]
        self.assertEqual(7, order['khachHang'])
        self.assertEqual({'$numberDecimal': '123.45'}, order['tongTien'])
        self.assertEqual(9, order['chiTiet'][0]['_id'])
        self.assertNotIn('donHang', order['chiTiet'][0])
        self.assertNotIn('don_hang_chi_tiet', result)
        self.assertIn({'_id': 'DonHangChiTiet', 'value': 9}, result['sequences'])
    def test_orphan_fails(self):
        with self.assertRaises(ValueError): convert({'don_hang_chi_tiet': [{'id': 1, 'don_hang_id': 2}]})
if __name__ == '__main__': unittest.main()
