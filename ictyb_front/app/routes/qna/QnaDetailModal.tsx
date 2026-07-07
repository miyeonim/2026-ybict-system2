import React, { useEffect, useState } from 'react';
import { Button } from '@/components/ui/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog';
import { fetchQnaDetail, deleteQna } from '~/hooks/qna/QnaController';

interface Props {
  open: boolean;
  noticeNo: number;
  onClose: () => void;
  onDeleteSuccess: () => void;
}

const QnaDetailModal: React.FC<Props> = ({ open, noticeNo, onClose, onDeleteSuccess }) => {
  const [data, setData] = useState<any>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open && noticeNo) {
      setLoading(true);
      fetchQnaDetail(noticeNo)
        .then(setData)
        .finally(() => setLoading(false));
    }
  }, [open, noticeNo]);

  const handleDelete = async () => {
    if (!confirm("정말 이 게시글을 삭제하시겠습니까?")) return;
    
    try {
      setLoading(true);
      await deleteQna(noticeNo);
      onDeleteSuccess(); // 성공 시 리스트 갱신 및 모달 닫기
    } catch (e) {
      alert("삭제 중 오류가 발생했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className="max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-lg font-bold text-[var(--sidebar-bg)]">
            Q&amp;A 상세
          </DialogTitle>
        </DialogHeader>

        <div className="py-4 min-h-[200px]">
          {loading ? (
            <div className="text-center py-10 text-slate-400">불러오는 중...</div>
          ) : data ? (
            <div className="flex flex-col gap-4">
              <div>
                <h3 className="text-xl font-bold text-slate-800">{data.noticeTitle}</h3>
                <p className="text-sm text-slate-500 mt-1">작성자: {data.regUserName} | 조회수: {data.viewCnt}</p>
              </div>
              <div className="border-t border-b py-4 text-slate-700 whitespace-pre-wrap">
                {data.noticeContents}
              </div>
            </div>
          ) : (
            <div className="text-center text-red-500">데이터를 불러올 수 없습니다.</div>
          )}
        </div>

        <DialogFooter className="gap-2">
          <Button variant="outline" onClick={onClose} disabled={loading}>
            닫기
          </Button>
          <Button 
            variant="destructive" 
            onClick={handleDelete} 
            disabled={loading}
          >
            삭제
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default QnaDetailModal;