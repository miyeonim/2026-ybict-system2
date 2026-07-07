import React, { useEffect, useState } from 'react';
import type { QnaListItem } from './QnaDto';
import { fetchQnaList } from '~/hooks/qna/QnaController';
import { 
  Table, TableBody, TableCell, TableHead, TableHeader, TableRow 
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { useAuthContext } from "@routes/common/jwt/AuthContext";    //사용자정보가져오기 

//modal
import QnaRegModal from '@routes/qna/list/QnaRegModal';
import QnaDetailModal from '../QnaDetailModal';


interface QnaListProps {
  onRowClick: (noticeNo: number) => void;
}

const QnaList: React.FC<QnaListProps> = ({ onRowClick }) => {
  const [list, setList]           = useState<QnaListItem[]>([]);
  const [loading, setLoading]     = useState(false);
  const [error, setError]         = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);   // 등록 모달 상태
  const [detailOpen, setDetailOpen] = useState(false);
  const [selectedNo, setSelectedNo] = useState<number | null>(null);

  // ── 목록 조회 ─────────────────────────────────────────────────
  const loadList = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchQnaList();
      setList(data);
    } catch (e: any) {
      setError(e.message ?? '목록 조회 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadList();
  }, []);

  // ── 등록 성공 콜백: 모달 닫고 목록 재조회 ─────────────────────
  //등록
  const handleRegisterSuccess = () => {
    setModalOpen(false);
    loadList();
  };

  // 조회
  const handleRowClick = (noticeNo: number) => {
    setSelectedNo(noticeNo);
    setDetailOpen(true);
  };

  const formatDate = (dt: string) => {
    if (!dt || dt.length < 8) return dt;
    return `${dt.slice(0, 4)}-${dt.slice(4, 6)}-${dt.slice(6, 8)}`;
  };
  

  // ── 테이블 바디 렌더링 ────────────────────────────────────────
  const renderTableBody = () => {
    if (loading) {
      return (
        <TableRow>
          <TableCell colSpan={5} className="text-center py-10 text-slate-400">
            <span className="inline-block w-5 h-5 border-2 border-slate-300 border-t-[var(--sidebar-bg)] rounded-full animate-spin mr-2 align-middle" />
            불러오는 중...
          </TableCell>
        </TableRow>
      );
    }

    if (list.length === 0) {
      return (
        <TableRow>
          <TableCell colSpan={5} className="text-center py-10 text-slate-400">
            등록된 Q&amp;A가 없습니다.
          </TableCell>
        </TableRow>
      );
    }

    return list.map((item, idx) => (
      <TableRow
        key={item.noticeNo}
        className="cursor-pointer hover:bg-slate-50 transition-colors"
        onClick={() => handleRowClick(item.noticeNo)}
      >
        <TableCell className="text-center">{list.length - idx}</TableCell>
        <TableCell className="font-medium text-[var(--sidebar-bg)]">
          {item.noticeTitle}
          {item.attachCount > 0 && (
            <span className="ml-2 text-xs bg-[var(--status-new)]/20 px-1.5 py-0.5 rounded text-[var(--status-done)]">
              📎 {item.attachCount}
            </span>
          )}
        </TableCell>
        <TableCell className="text-center">{item.regUserName}</TableCell>
        <TableCell className="text-center">{formatDate(item.regDt)}</TableCell>
        <TableCell className="text-center">{item.viewCnt}</TableCell>
      </TableRow>
    ));
  };

  return (
    <div className="w-full bg-[var(--page-bg)] p-6 rounded-xl border border-slate-200">
      {/* 헤더 */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-bold text-[var(--sidebar-bg)]">Q&amp;A</h2>
        <Button
          onClick={() => setModalOpen(true)}
          className="bg-[var(--sidebar-bg)] hover:bg-[var(--accent-main)] text-white shadow-md transition-all"
        >
          + 등록
        </Button>
      </div>

      {/* 에러 */}
      {error && (
        <div className="bg-red-50 text-red-600 p-4 rounded-lg mb-4 text-sm border border-red-200">
          {error}
        </div>
      )}

      {/* 테이블 */}
      <div className="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
        <Table>
          <TableHeader className="bg-slate-50">
            <TableRow>
              <TableHead className="w-[80px] text-center text-[var(--sidebar-bg)] font-bold">번호</TableHead>
              <TableHead className="text-[var(--sidebar-bg)] font-bold">제목</TableHead>
              <TableHead className="w-[100px] text-center text-[var(--sidebar-bg)] font-bold">작성자</TableHead>
              <TableHead className="w-[120px] text-center text-[var(--sidebar-bg)] font-bold">등록일</TableHead>
              <TableHead className="w-[80px] text-center text-[var(--sidebar-bg)] font-bold">조회</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {renderTableBody()}
          </TableBody>
        </Table>
      </div>

      <div className="text-right text-xs text-slate-400 mt-2">총 {list.length}건</div>

      {/* 등록 모달 */}
      <QnaRegModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onSuccess={handleRegisterSuccess}
      />

      {/* 상세 조회 모달 */}
      {selectedNo !== null && (
        <QnaDetailModal
          open={detailOpen}
          noticeNo={selectedNo}
          onClose={() => setDetailOpen(false)}
          onDeleteSuccess={() => {
            setDetailOpen(false);
            loadList();
          }}
        />
      )}
    </div>
  );
};

export default QnaList;
