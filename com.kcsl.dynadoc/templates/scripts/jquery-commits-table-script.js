    $(document).ready(function() {
        var dt = $('#commits-table').DataTable( {
            "lengthMenu": [[10, 25, 50, -1], [10, 25, 50, "All"]],
            "columns": 
            [
                {
                    "orderable":      false,
                },
                { "data": "commitId" },
                { "data": "commiter" },
                { "data": "dataTime" },
                { "data": "summary" },
                { "bSearchable": false, "orderable": false, "data": "commitDetails" },
            ],
            "order": [[1, 'asc']]
        } );

        var detailRows = [];
     
        $('#commits-table tbody').on( 'click', 'tr td.details-control', function () {
            var tr = $(this).closest('tr');
            var row = dt.row( tr );
            var idx = $.inArray( tr.attr('id'), detailRows );
     
            if ( row.child.isShown() ) {
                tr.removeClass( 'details' );
                row.child.hide();
     
                detailRows.splice( idx, 1 );
            }
            else {
                tr.addClass( 'details' );
                row.child( row.data().summary ).show();
     
                if ( idx === -1 ) {
                    detailRows.push( tr.attr('id') );
                }
            }
        } );
     
        dt.on( 'draw', function () {
            $.each( detailRows, function ( i, id ) {
                $('#'+id+' td.details-control').trigger( 'click' );
            } );
        } );
    } );  