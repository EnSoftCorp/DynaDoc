    $(document).ready(function() {
        var dt = $('#fields-table').DataTable( {
            "columns": 
            [
                {
                    "orderable":      false,
                },
                { "data": "accessor" },
                { "data": "static" },
                { "data": "final" },
                { "data": "type" },
                { "data": "name" },
                { "data": "externally_used" },
                { "bSearchable": false, "orderable": false, "data": "usage_example" },
                { "orderable": false, "data": "comments" }
            ],
            "order": [[1, 'asc']]
        } );


        var detailRows = [];
     
        $('#fields-table tbody').on( 'click', 'tr td.details-control', function () {
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
                row.child( row.data().comments ).show();
     
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